package com.donatrack.notificaciones.infrastructure.controller;

import com.donatrack.notificaciones.domain.service.NotificadorService;
import com.donatrack.notificaciones.domain.service.NotificacionEmail;
import com.donatrack.notificaciones.domain.service.NotificacionSMS;
import com.donatrack.notificaciones.domain.service.NotificacionWhatsApp;
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
