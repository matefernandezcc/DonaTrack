package com.donatrack.incentivos.infrastructure.adapters.in.api;

import com.donatrack.incentivos.domain.entities.Insignia;
import com.donatrack.incentivos.domain.entities.misiones.TipoMetricaMision;
import java.util.UUID;

public record MisionProgresoDTO(
    UUID id,
    String nombre,
    Insignia recompensa,
    TipoMetricaMision tipoMetrica,
    int objetivo,
    int valorActual,
    int porcentaje,
    int faltante,
    boolean completada,
    boolean bloqueada,
    String unidadLabel) {}
