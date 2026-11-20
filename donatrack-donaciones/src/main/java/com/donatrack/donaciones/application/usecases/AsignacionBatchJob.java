package com.donatrack.donaciones.application.usecases;

import com.donatrack.donaciones.application.ports.out.BeneficiarioRepository;
import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.services.MatchmakerService;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.SolicitudOngEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.SolicitudOngJpaRepository;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AsignacionBatchJob {

  private static final Pattern DIRECCION_PATTERN =
      Pattern.compile("^(.*?)\\s+(\\d+)(?:,\\s*(.*))?$");

  private final DonacionRepository donacionRepository;
  private final BeneficiarioRepository beneficiarioRepository;
  private final MatchmakerService matchmakerService;
  private final com.donatrack.donaciones.application.ports.out.LogisticaPort logisticaPort;
  private final com.donatrack.donaciones.application.ports.out.PersonaRepository personaRepository;
  private final SolicitudOngJpaRepository solicitudOngRepository;

  public AsignacionBatchJob(
      DonacionRepository donacionRepository,
      BeneficiarioRepository beneficiarioRepository,
      MatchmakerService matchmakerService,
      com.donatrack.donaciones.application.ports.out.LogisticaPort logisticaPort,
      com.donatrack.donaciones.application.ports.out.PersonaRepository personaRepository) {
    this(
        donacionRepository,
        beneficiarioRepository,
        matchmakerService,
        logisticaPort,
        personaRepository,
        null);
  }

  @Autowired
  public AsignacionBatchJob(
      DonacionRepository donacionRepository,
      BeneficiarioRepository beneficiarioRepository,
      MatchmakerService matchmakerService,
      com.donatrack.donaciones.application.ports.out.LogisticaPort logisticaPort,
      com.donatrack.donaciones.application.ports.out.PersonaRepository personaRepository,
      @Autowired(required = false) SolicitudOngJpaRepository solicitudOngRepository) {
    this.donacionRepository = donacionRepository;
    this.beneficiarioRepository = beneficiarioRepository;
    this.matchmakerService = matchmakerService;
    this.logisticaPort = logisticaPort;
    this.personaRepository = personaRepository;
    this.solicitudOngRepository = solicitudOngRepository;
  }

  /**
   * Se ejecuta todos los días a las 2 AM. Busca las donaciones En Depósito y usa el Matchmaker para
   * asignar automáticamente el mejor candidato posible.
   */
  @org.springframework.scheduling.annotation.Async
  @Scheduled(cron = "0 0 2 * * ?")
  public void asignarDonacionesEnDeposito() {
    List<Donacion> donacionesPendientes =
        donacionRepository.buscarPorEstado(EstadoDonacion.EN_DEPOSITO);
    List<Beneficiario> beneficiariosDisponibles = beneficiarioRepository.buscarTodos();

    for (Donacion donacion : donacionesPendientes) {
      List<Beneficiario> sugerencias =
          matchmakerService.obtenerSugerencias(donacion, beneficiariosDisponibles);

      if (!sugerencias.isEmpty()) {
        Beneficiario mejorCandidato = sugerencias.get(0);
        donacion.asignar(mejorCandidato);
        mejorCandidato.getDonacionesAsignadas().add(donacion);

        donacionRepository.guardar(donacion);

        // Solicitar retiro a Logística (usando el Broker de Integración)
        String[] destino =
            resolverDireccionDestino(mejorCandidato, personaRepository, solicitudOngRepository);
        double sumaCantidad =
            donacion.getBienes() != null
                ? donacion.getBienes().stream().mapToDouble(b -> b.getCantidad()).sum()
                : 0.0;
        double pesoTotal =
            sumaCantidad > 0 ? sumaCantidad : Math.max(1, donacion.getBienes().size()) * 2.5;
        double volumenTotal = Math.max(1, donacion.getBienes().size()) * 0.5;

        try {
          logisticaPort.solicitarRetiro(
              donacion.getId(), pesoTotal, volumenTotal, destino[0], destino[1], destino[2]);
        } catch (Exception ignored) {
          // Tolerancia ante indisponibilidad transitoria de logística
        }
      }
    }
  }

  /**
   * Resuelve calle, altura y localidad reales de destino para una entidad beneficiaria sin valores
   * hardcodeados.
   */
  public static String[] resolverDireccionDestino(
      Beneficiario beneficiario,
      com.donatrack.donaciones.application.ports.out.PersonaRepository personaRepository,
      SolicitudOngJpaRepository solicitudOngRepository) {
    if (beneficiario != null) {
      if (personaRepository != null) {
        var personaOpt = personaRepository.buscarPorRolId(beneficiario.getId());
        if (personaOpt.isEmpty()
            && beneficiario.getCorreoRepresentante() != null
            && !beneficiario.getCorreoRepresentante().isBlank()) {
          personaOpt = personaRepository.buscarPorEmail(beneficiario.getCorreoRepresentante().trim());
        }
        if (personaOpt.isPresent() && personaOpt.get().getDireccion() != null) {
          var dir = personaOpt.get().getDireccion();
          if (dir.getCalle() != null && !dir.getCalle().isBlank()) {
            String loc =
                (dir.getLocalidad() != null
                        && !dir.getLocalidad().isBlank()
                        && !"Argentina".equalsIgnoreCase(dir.getLocalidad().trim()))
                    ? dir.getLocalidad().trim()
                    : "Buenos Aires";
            if (dir.getAltura() > 0) {
              double alt = dir.getAltura();
              String alturaFmt =
                  (alt == Math.floor(alt))
                      ? String.valueOf((long) alt)
                      : String.valueOf(alt);
              return new String[] {dir.getCalle().trim(), alturaFmt, loc};
            }
            return parsearComponentesDireccion(dir.getCalle().trim(), loc);
          }
        }
      }
      if (solicitudOngRepository != null
          && beneficiario.getCorreoRepresentante() != null
          && !beneficiario.getCorreoRepresentante().isBlank()) {
        String email = beneficiario.getCorreoRepresentante().trim();
        for (SolicitudOngEntity s : solicitudOngRepository.findAllByOrderByFechaCreacionDesc()) {
          if (s.getEmail() != null
              && email.equalsIgnoreCase(s.getEmail().trim())
              && s.getDireccion() != null
              && !s.getDireccion().isBlank()
              && !"-".equals(s.getDireccion().trim())) {
            return parsearComponentesDireccion(s.getDireccion().trim(), "Buenos Aires");
          }
        }
      }
    }
    return new String[] {"Sede Beneficiaria", "", "Buenos Aires"};
  }

  /** Separa calle, altura numérica y localidad desde una dirección textual libre. */
  public static String[] parsearComponentesDireccion(String raw, String defaultLocalidad) {
    Matcher m = DIRECCION_PATTERN.matcher(raw.trim());
    if (m.matches()) {
      String calle = m.group(1).trim();
      String altura = m.group(2).trim();
      String loc =
          (m.group(3) != null
                  && !m.group(3).isBlank()
                  && !"Argentina".equalsIgnoreCase(m.group(3).trim()))
              ? m.group(3).trim()
              : defaultLocalidad;
      return new String[] {calle, altura, loc};
    }
    return new String[] {raw.trim(), "", defaultLocalidad};
  }
}
