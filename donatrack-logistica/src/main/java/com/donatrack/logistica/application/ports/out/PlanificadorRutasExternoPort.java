package com.donatrack.logistica.application.ports.out;

import com.donatrack.logistica.domain.entities.Camion;
import java.util.List;
import java.util.UUID;

public interface PlanificadorRutasExternoPort {
    void solicitarPlanificacion(UUID solicitudId, List<UUID> idsDonaciones, List<Camion> camiones);
}
