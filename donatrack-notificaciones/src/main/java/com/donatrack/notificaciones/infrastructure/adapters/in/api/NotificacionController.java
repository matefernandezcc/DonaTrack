package com.donatrack.notificaciones.infrastructure.adapters.in.api;

import com.donatrack.notificaciones.application.usecases.NotificadorService;
import com.donatrack.notificaciones.infrastructure.adapters.out.client.NotificacionRequest;

import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Enviar notificación", description = "Envía una notificación a un destinatario por el medio especificado (email, SMS o WhatsApp)")
    @ApiResponse(responseCode = "200", description = "Notificación enviada")
    @PostMapping("/mensajes")
    public ResponseEntity<Void> enviarNotificacion(@RequestBody NotificacionRequest request) {
        notificadorService.enviarNotificacion(request.destinatario(), request.mensaje(), request.medio());
        return ResponseEntity.ok().build();
    }
}
