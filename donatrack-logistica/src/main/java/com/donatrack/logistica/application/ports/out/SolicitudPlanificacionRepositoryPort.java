package com.donatrack.logistica.application.ports.out;

import com.donatrack.logistica.domain.entities.planificacion.SolicitudPlanificacion;

import java.util.Optional;
import java.util.UUID;

public interface SolicitudPlanificacionRepositoryPort {
    void guardar(SolicitudPlanificacion solicitud);

    Optional<SolicitudPlanificacion> buscarPorId(UUID id);
}
