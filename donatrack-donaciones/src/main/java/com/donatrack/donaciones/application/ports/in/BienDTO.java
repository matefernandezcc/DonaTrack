package com.donatrack.donaciones.application.ports.in;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Datos de un bien individual dentro de una recepción de donación")
public record BienDTO(
    @Schema(description = "Descripción del bien", example = "Paquete de arroz 1kg", requiredMode = Schema.RequiredMode.REQUIRED)
        String descripcion,
    @Schema(description = "Cantidad del bien", example = "10.0", requiredMode = Schema.RequiredMode.REQUIRED)
        double cantidad,
    @Schema(description = "Unidad de medición (ej: KILOS, UNIDADES, LITROS)", example = "KILOS", requiredMode = Schema.RequiredMode.REQUIRED)
        String unidadMedicion,
    @Schema(description = "true si el bien es usado, false si es nuevo", example = "false")
        boolean esUsado,
    @Schema(description = "Fecha de vencimiento del bien (null si no aplica)", example = "2027-06-01", nullable = true)
        LocalDate fechaVencimiento,
    @Schema(description = "Nombre de la subcategoría del bien", example = "No Perecederos", requiredMode = Schema.RequiredMode.REQUIRED)
        String nombreSubcategoria) {}
