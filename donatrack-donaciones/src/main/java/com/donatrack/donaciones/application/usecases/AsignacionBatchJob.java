package com.donatrack.donaciones.application.usecases;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.application.ports.out.BeneficiarioRepository;
import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.domain.services.MatchmakerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsignacionBatchJob {

    private final DonacionRepository donacionRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final MatchmakerService matchmakerService;
    private final com.donatrack.donaciones.application.ports.out.LogisticaPort logisticaPort;
    private final com.donatrack.donaciones.application.ports.out.PersonaRepository personaRepository;

    public AsignacionBatchJob(DonacionRepository donacionRepository,
                              BeneficiarioRepository beneficiarioRepository,
                              MatchmakerService matchmakerService,
                              com.donatrack.donaciones.application.ports.out.LogisticaPort logisticaPort,
                              com.donatrack.donaciones.application.ports.out.PersonaRepository personaRepository) {
        this.donacionRepository = donacionRepository;
        this.beneficiarioRepository = beneficiarioRepository;
        this.matchmakerService = matchmakerService;
        this.logisticaPort = logisticaPort;
        this.personaRepository = personaRepository;
    }

    /**
     * Se ejecuta todos los días a las 2 AM.
     * Busca las donaciones En Depósito y usa el Matchmaker para asignar automáticamente
     * el mejor candidato posible.
     */
    @org.springframework.scheduling.annotation.Async
    @Scheduled(cron = "0 0 2 * * ?")
    public void asignarDonacionesEnDeposito() {
        List<Donacion> donacionesPendientes = donacionRepository.buscarPorEstado(EstadoDonacion.EN_DEPOSITO);
        List<Beneficiario> beneficiariosDisponibles = beneficiarioRepository.buscarTodos();

        for (Donacion donacion : donacionesPendientes) {
            List<Beneficiario> sugerencias = matchmakerService.obtenerSugerencias(donacion, beneficiariosDisponibles);

            if (!sugerencias.isEmpty()) {
                Beneficiario mejorCandidato = sugerencias.get(0);
                donacion.asignar(mejorCandidato);
                mejorCandidato.getDonacionesAsignadas().add(donacion);
                
                donacionRepository.guardar(donacion);
                
                // Solicitar retiro a Logística (usando el Broker de Integración)
                var personaOpt = personaRepository.buscarPorRolId(mejorCandidato.getId());
                if (personaOpt.isPresent() && personaOpt.get().getDireccion() != null) {
                    var dir = personaOpt.get().getDireccion();
                    // Valores mockeados para peso/volumen para simplificar, en un caso real se calcularían
                    double pesoTotal = donacion.getBienes().size() * 2.5; 
                    double volumenTotal = donacion.getBienes().size() * 0.5;
                    
                    logisticaPort.solicitarRetiro(
                            donacion.getId(), 
                            pesoTotal, 
                            volumenTotal, 
                            dir.getCalle(), 
                            String.valueOf(dir.getAltura()), 
                            dir.getLocalidad()
                    );
                }
            }
        }
    }
}
