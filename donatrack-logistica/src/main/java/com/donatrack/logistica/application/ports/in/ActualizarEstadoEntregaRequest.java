package com.donatrack.logistica.application.ports.in;

import com.donatrack.logistica.domain.entities.EstadoEntrega;
import java.util.List;

public record ActualizarEstadoEntregaRequest(
    EstadoEntrega estado,
    List<String> fotos,
    String patenteCamion,
    String motivo
) {}
