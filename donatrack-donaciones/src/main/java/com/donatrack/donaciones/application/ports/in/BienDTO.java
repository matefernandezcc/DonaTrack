package com.donatrack.donaciones.application.ports.in;

import java.time.LocalDate;

public record BienDTO(
    String descripcion,
    double cantidad,
    String unidadMedicion,
    boolean esUsado,
    LocalDate fechaVencimiento,
    String nombreSubcategoria
) {}
