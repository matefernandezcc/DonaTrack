package com.donatrack.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.UUID;

public record RecepcionDepositoRequest(
    @NotNull(message = "El donante es obligatorio") UUID idDonante,
    @NotBlank(message = "Descripción obligatoria") String descripcion,
    @NotNull @Positive(message = "Cantidad debe ser mayor a cero") Double cantidad,
    @NotBlank String unidadMedicion,
    @NotBlank String nombreSubcategoria,
    boolean esUsado,
    LocalDate fechaVencimiento
) {}
