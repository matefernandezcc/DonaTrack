package com.donatrack.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Estructura estándar de respuesta de error en la API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    @Schema(description = "Fecha y hora en que ocurrió el error", example = "2026-09-25T03:38:52")
        LocalDateTime timestamp,
    @Schema(description = "Código de estado HTTP", example = "400") int status,
    @Schema(description = "Razón o nombre del error HTTP", example = "Bad Request") String error,
    @Schema(
            description = "Descripción detallada del error",
            example = "La persona no tiene rol Donante")
        String message,
    @Schema(
            description = "Ruta o URI del endpoint que generó el error",
            example = "/api/recepciones")
        String path,
    @Schema(
            description = "Lista de errores detallados por campo (si corresponde a validación)",
            nullable = true)
        List<CampoError> detalles) {
  public ErrorResponse(
      LocalDateTime timestamp, int status, String error, String message, String path) {
    this(timestamp, status, error, message, path, null);
  }

  @Schema(description = "Detalle de error en un campo específico de la solicitud")
  public record CampoError(
      @Schema(description = "Nombre del campo con error", example = "tipo") String campo,
      @Schema(description = "Mensaje o causa del error", example = "No debe ser nulo")
          String mensaje) {}
}
