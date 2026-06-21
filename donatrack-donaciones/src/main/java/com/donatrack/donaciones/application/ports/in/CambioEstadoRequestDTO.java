package com.donatrack.donaciones.application.ports.in;

import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import java.util.UUID;

public record CambioEstadoRequestDTO(
    EstadoDonacion nuevoEstado,
    String observacion,
    UUID idUsuario
) {}
