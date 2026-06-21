package com.donatrack.logistica.application.ports.in;

import java.util.List;
import java.util.UUID;

public record PlanificarRutasRequest(
    List<UUID> idsDonaciones
) {}
