package com.donatrack.donaciones.application.ports.in;

import java.util.UUID;

public record EntregadaRequestDTO(
    UUID idDonante
) {}
