package com.donatrack.donaciones.application.service;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.enums.EstadoDonacionEnum;
import com.donatrack.donaciones.domain.model.roles.Beneficiario;
import com.donatrack.donaciones.domain.repository.BeneficiarioRepository;
import com.donatrack.donaciones.domain.repository.DonacionRepository;
import com.donatrack.donaciones.domain.service.matchmaking.MatchmakerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsignacionBatchJob {

    private final DonacionRepository donacionRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final MatchmakerService matchmakerService;

    public AsignacionBatchJob(DonacionRepository donacionRepository,
                              BeneficiarioRepository beneficiarioRepository,
                              MatchmakerService matchmakerService) {
        this.donacionRepository = donacionRepository;
        this.beneficiarioRepository = beneficiarioRepository;
        this.matchmakerService = matchmakerService;
    }

    /**
     * Se ejecuta todos los días a las 2 AM.
     * Busca las donaciones En Depósito y usa el Matchmaker para asignar automáticamente
     * el mejor candidato posible.
     */
    @org.springframework.scheduling.annotation.Async
    @Scheduled(cron = "0 0 2 * * ?")
    public void asignarDonacionesEnDeposito() {
        List<Donacion> donacionesPendientes = donacionRepository.buscarPorEstado(EstadoDonacionEnum.EN_DEPOSITO);
        List<Beneficiario> beneficiariosDisponibles = beneficiarioRepository.buscarTodos();

        for (Donacion donacion : donacionesPendientes) {
            List<Beneficiario> sugerencias = matchmakerService.obtenerSugerencias(donacion, beneficiariosDisponibles);

            if (!sugerencias.isEmpty()) {
                Beneficiario mejorCandidato = sugerencias.get(0);
                donacion.asignar(mejorCandidato);
                // Si la relación es bidireccional, deberíamos agregarlo al beneficiario también.
                mejorCandidato.getDonacionesAsignadas().add(donacion);
                
                donacionRepository.guardar(donacion);
                // Aquí se podría guardar el beneficiario si fuera necesario según el ORM
            }
        }
    }
}
