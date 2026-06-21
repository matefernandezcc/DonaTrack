package com.donatrack.donaciones.application.port.in;

import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import java.util.UUID;

public record DonacionResponseDTO(
    UUID id,
    EstadoDonacion estadoActual,
    UUID idEntidadAsignada
) {}
