package com.donatrack.donaciones.application.port.in;

import java.util.List;

public record DonacionRequestDTO(
    List<BienDTO> bienes
) {}
