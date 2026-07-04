package com.donatrack.donaciones.application.usecases;

import com.donatrack.common.events.DonacionReplanificadaEvent;
import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ReplanificarEntregaUseCase {

    private final DonacionRepository donacionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReplanificarEntregaUseCase(DonacionRepository donacionRepository, ApplicationEventPublisher eventPublisher) {
        this.donacionRepository = donacionRepository;
        this.eventPublisher = eventPublisher;
    }

    public void replanificar(UUID idDonacion, String adminResponsableId) {
        Optional<Donacion> donacionOpt = donacionRepository.buscarPorId(idDonacion);
        if (donacionOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró la donación para replanificar.");
        }

        Donacion donacion = donacionOpt.get();
        if (donacion.getEstado() != EstadoDonacion.ENTREGA_FALLIDA) {
            throw new IllegalStateException("Solo se puede replanificar una donación en estado ENTREGA_FALLIDA");
        }

        // Vuelve al estado ASIGNADA para que el sistema de ruteo pueda volver a considerarla
        donacion.cambiarEstado(EstadoDonacion.ASIGNADA, "Replanificación solicitada", adminResponsableId);
        donacionRepository.guardar(donacion);

        DonacionReplanificadaEvent event = new DonacionReplanificadaEvent(idDonacion);
        eventPublisher.publishEvent(event);
    }
}
