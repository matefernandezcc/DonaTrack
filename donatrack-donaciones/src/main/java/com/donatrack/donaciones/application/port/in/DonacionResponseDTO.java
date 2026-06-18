package com.donatrack.donaciones.application.port.in;

import com.donatrack.donaciones.domain.model.enums.EstadoDonacionEnum;
import java.util.UUID;

public record DonacionResponseDTO(
    UUID id,
    EstadoDonacionEnum estadoActual,
    UUID idEntidadAsignada
) {}
