package com.donatrack.notificaciones.infrastructure.adapters.in.api;

import com.donatrack.notificaciones.application.usecases.NotificadorService;
import com.donatrack.notificaciones.infrastructure.adapters.out.client.NotificacionRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final NotificadorService notificadorService;

    public NotificacionController(NotificadorService notificadorService) {
        this.notificadorService = notificadorService;
    }

    @PostMapping("/enviar")
    public ResponseEntity<Void> enviarNotificacion(@RequestBody NotificacionRequest request) {
        notificadorService.enviarNotificacion(request.destinatario(), request.mensaje(), request.medio());
        return ResponseEntity.ok().build();
    }
}
