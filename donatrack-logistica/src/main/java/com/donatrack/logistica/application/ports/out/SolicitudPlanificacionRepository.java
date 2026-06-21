package com.donatrack.logistica.application.ports.out;

import com.donatrack.logistica.domain.entities.SolicitudPlanificacion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolicitudPlanificacionRepository {
    void guardar(SolicitudPlanificacion solicitud);
    Optional<SolicitudPlanificacion> buscarPorId(UUID id);
    List<SolicitudPlanificacion> buscarTodas();
}
