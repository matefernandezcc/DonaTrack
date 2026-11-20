package com.donatrack.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record RecepcionDepositoRequest(
    UUID idDonante,
    @NotBlank(message = "Descripción obligatoria") String descripcion,
    @NotNull @Positive(message = "Cantidad debe ser mayor a cero") Double cantidad,
    @NotBlank(message = "Unidad de medición obligatoria") String unidadMedicion,
    @NotBlank(message = "Subcategoría obligatoria") String nombreSubcategoria,
    Boolean esUsado,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVencimiento) {

  public static final UUID DONANTE_ANONIMO_UUID =
      UUID.fromString("a0000000-0000-4000-8000-000000000000");

  public UUID idDonanteResolvido() {
    return idDonante != null ? idDonante : DONANTE_ANONIMO_UUID;
  }

  public boolean esUsadoValue() {
    return Boolean.TRUE.equals(esUsado);
  }
}
