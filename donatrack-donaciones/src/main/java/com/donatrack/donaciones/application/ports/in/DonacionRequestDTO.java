package com.donatrack.donaciones.application.ports.in;

import java.util.List;

public record DonacionRequestDTO(
    List<BienDTO> bienes
) {}
