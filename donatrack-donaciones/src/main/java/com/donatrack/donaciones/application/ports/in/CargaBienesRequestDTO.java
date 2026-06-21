package com.donatrack.donaciones.application.ports.in;

import java.util.List;
import java.util.UUID;

public record CargaBienesRequestDTO(
    UUID idDonante,
    UUID idAdministrador,
    List<BienDTO> bienesBrutos
) {}
