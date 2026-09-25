package com.donatrack.notificaciones.infrastructure.adapters.in.api;

import com.donatrack.common.dto.ErrorResponse;
import com.donatrack.notificaciones.application.usecases.NotificadorService;
import com.donatrack.notificaciones.infrastructure.adapters.out.client.NotificacionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Notificaciones", description = "Envío de notificaciones por email, SMS y WhatsApp")
public class NotificacionController {

  private final NotificadorService notificadorService;

  public NotificacionController(NotificadorService notificadorService) {
    this.notificadorService = notificadorService;
  }

  @Operation(
      summary = "Enviar notificación",
      description =
          "Envía una notificación a un destinatario por el medio especificado (email, SMS o WhatsApp)")
  @ApiResponse(responseCode = "200", description = "Notificación enviada exitosamente")
  @ApiResponse(
      responseCode = "400",
      description = "Datos de notificación inválidos o medio no soportado",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Error interno en el envío de notificación",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/mensajes")
  public ResponseEntity<Void> enviarNotificacion(@RequestBody NotificacionRequest request) {
    notificadorService.enviarNotificacion(
        request.destinatario(), request.mensaje(), request.medio());
    return ResponseEntity.ok().build();
  }
}
