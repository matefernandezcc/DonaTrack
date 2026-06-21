package com.donatrack.logistica.application.ports.in;

import com.donatrack.logistica.domain.entities.SolicitudPlanificacion;
import java.util.List;
import java.util.UUID;

public interface PlanificarRutasUseCase {
    SolicitudPlanificacion planificar(List<UUID> idsDonaciones);
}
