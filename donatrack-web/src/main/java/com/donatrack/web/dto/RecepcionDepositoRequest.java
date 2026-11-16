package com.donatrack.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record RecepcionDepositoRequest(
    @NotNull(message = "El donante es obligatorio") UUID idDonante,
    @NotBlank(message = "Descripción obligatoria") String descripcion,
    @NotNull @Positive(message = "Cantidad debe ser mayor a cero") Double cantidad,
    @NotBlank String unidadMedicion,
    @NotBlank String nombreSubcategoria,
    boolean esUsado,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVencimiento) {}
