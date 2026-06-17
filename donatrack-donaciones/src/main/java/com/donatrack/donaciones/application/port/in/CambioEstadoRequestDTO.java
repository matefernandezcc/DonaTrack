package com.donatrack.donaciones.application.port.in;

import com.donatrack.donaciones.domain.model.enums.EstadoDonacionEnum;
import java.util.UUID;

public record CambioEstadoRequestDTO(
    EstadoDonacionEnum nuevoEstado,
    String observacion,
    UUID idUsuario
) {}
