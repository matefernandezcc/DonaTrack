package com.donatrack.donaciones.application.usecases;

import com.donatrack.common.events.EntregaRealizadaEvent;
import com.donatrack.common.events.NotificacionEntregaExitosaEvent;
import com.donatrack.common.events.NotificacionInicioRutaEvent;
import com.donatrack.donaciones.application.ports.out.DonacionOriginalRepository;
import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmarRecepcionYNotificarUseCase {

  private final DonacionRepository donacionRepository;
  private final DonacionOriginalRepository recepcionRepository;
  private final PersonaRepository personaRepository;
  private final ApplicationEventPublisher eventPublisher;

  @org.springframework.beans.factory.annotation.Autowired(required = false)
  private com.donatrack.donaciones.infrastructure.adapters.out.client.IncentivoClient incentivoClient;

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(ConfirmarRecepcionYNotificarUseCase.class);

  public ConfirmarRecepcionYNotificarUseCase(
      DonacionRepository donacionRepository,
      DonacionOriginalRepository recepcionRepository,
      PersonaRepository personaRepository,
      ApplicationEventPublisher eventPublisher) {
    this.donacionRepository = donacionRepository;
    this.recepcionRepository = recepcionRepository;
    this.personaRepository = personaRepository;
    this.eventPublisher = eventPublisher;
  }

  public ConfirmarRecepcionYNotificarUseCase(
      DonacionRepository donacionRepository,
      DonacionOriginalRepository recepcionRepository,
      PersonaRepository personaRepository,
      ApplicationEventPublisher eventPublisher,
      com.donatrack.donaciones.infrastructure.adapters.out.client.IncentivoClient incentivoClient) {
    this(donacionRepository, recepcionRepository, personaRepository, eventPublisher);
    this.incentivoClient = incentivoClient;
  }

  @Transactional
  public void procesar(EntregaRealizadaEvent event) {
    Optional<Donacion> donacionOpt = donacionRepository.buscarPorId(event.getIdDonacion());
    if (donacionOpt.isEmpty()) return;

    Donacion donacion = donacionOpt.get();
    Beneficiario beneficiario = donacion.getEntidadAsignada();

    if (beneficiario != null) {
      // Pasamos las URLs de fotos directamente como List<String>
      boolean confirmado = beneficiario.confirmarRecepcion(donacion, event.getFotos());
      if (confirmado) {
        donacionRepository.guardar(donacion);
      }
    }

    if (incentivoClient != null) {
      try {
        java.util.UUID idDonante = null;
        var recepcionOpt = recepcionRepository.buscarPorIdDonacion(donacion.getId());
        if (recepcionOpt.isPresent() && recepcionOpt.get().getDonante() != null) {
          idDonante = recepcionOpt.get().getDonante().getId();
        }
        if (idDonante != null) {
          int cantidadBienes = donacion.getBienes() != null ? donacion.getBienes().size() : 1;
          List<String> categorias = donacion.getCategoriasString();
          if (categorias == null || categorias.isEmpty()) {
            categorias = List.of("Alimentos");
          }
          java.util.UUID idEntidad = beneficiario != null ? beneficiario.getId() : null;
          com.donatrack.common.dto.ActividadDonacionDTO dto =
              new com.donatrack.common.dto.ActividadDonacionDTO(
                  donacion.getId(), idDonante, cantidadBienes, categorias, idEntidad, java.time.LocalDate.now());
          incentivoClient.registrarActividadDonacionExitosa(idDonante, dto);
          log.info("Incentivos actualizados para donante {} tras entrega exitosa de donación {}", idDonante, donacion.getId());
        }
      } catch (Exception ex) {
        log.warn("No se pudo notificar a incentivos al confirmar entrega: {}", ex.getMessage());
      }
    }

    List<NotificacionInicioRutaEvent.ContactoInfo> contactosDonantes =
        extraerContactosDonante(donacion);
    List<NotificacionInicioRutaEvent.ContactoInfo> contactosEntidades =
        extraerContactosEntidad(beneficiario);

    NotificacionEntregaExitosaEvent notificacionEvent =
        new NotificacionEntregaExitosaEvent(
            event.getIdDonacion(),
            contactosDonantes,
            contactosEntidades,
            event.getFechaHora(),
            event.getPatenteCamion(),
            event.getFotos());

    eventPublisher.publishEvent(notificacionEvent);
  }

  private List<NotificacionInicioRutaEvent.ContactoInfo> extraerContactosDonante(
      Donacion donacion) {
    List<NotificacionInicioRutaEvent.ContactoInfo> contactos = new ArrayList<>();
    Optional<DonacionOriginal> recepcionOpt =
        recepcionRepository.buscarPorIdDonacion(donacion.getId());
    if (recepcionOpt.isPresent()) {
      Donante donante = recepcionOpt.get().getDonante();
      if (donante != null) {
        Optional<Persona> personaDonante = personaRepository.buscarPorRolId(donante.getId());
        if (personaDonante.isPresent()) {
          contactos.addAll(extraerContactos(personaDonante.get(), "DONANTE"));
        }
      }
    }
    return contactos;
  }

  private List<NotificacionInicioRutaEvent.ContactoInfo> extraerContactosEntidad(
      Beneficiario beneficiario) {
    List<NotificacionInicioRutaEvent.ContactoInfo> contactos = new ArrayList<>();
    if (beneficiario != null) {
      Optional<Persona> personaBeneficiario =
          personaRepository.buscarPorRolId(beneficiario.getId());
      if (personaBeneficiario.isPresent()) {
        contactos.addAll(extraerContactos(personaBeneficiario.get(), "ENTIDAD_BENEFICIARIA"));
      }
    }
    return contactos;
  }

  private List<NotificacionInicioRutaEvent.ContactoInfo> extraerContactos(
      Persona persona, String rol) {
    List<NotificacionInicioRutaEvent.ContactoInfo> infos = new ArrayList<>();
    Contacto contacto = persona.getContacto();
    if (contacto == null) return infos;

    if (contacto.getMedioPredeterminado() != null) {
      switch (contacto.getMedioPredeterminado()) {
        case CORREO:
          if (contacto.getCorreoElectronico() != null
              && !contacto.getCorreoElectronico().isEmpty()) {
            infos.add(
                new NotificacionInicioRutaEvent.ContactoInfo(
                    contacto.getCorreoElectronico(), "EMAIL", rol));
          }
          break;
        case TELEFONO:
          if (contacto.getTelefono() != null && !contacto.getTelefono().isEmpty()) {
            infos.add(
                new NotificacionInicioRutaEvent.ContactoInfo(contacto.getTelefono(), "SMS", rol));
          }
          break;
        case WHATSAPP:
          if (contacto.getWhatsapp() != null && !contacto.getWhatsapp().isEmpty()) {
            infos.add(
                new NotificacionInicioRutaEvent.ContactoInfo(
                    contacto.getWhatsapp(), "WHATSAPP", rol));
          }
          break;
      }
    } else if (contacto.getCorreoElectronico() != null
        && !contacto.getCorreoElectronico().isEmpty()) {
      infos.add(
          new NotificacionInicioRutaEvent.ContactoInfo(
              contacto.getCorreoElectronico(), "EMAIL", rol));
    }
    return infos;
  }
}
