package com.donatrack.logistica.application.ports.in;

import com.donatrack.logistica.domain.entities.RutaDeReparto;
import java.util.List;
import java.util.UUID;

public record PlanificacionCallbackRequestDTO(
    UUID solicitudId,
    List<RutaDeReparto> rutas
) {}
