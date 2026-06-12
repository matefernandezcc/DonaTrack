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
        // Seleccionar medio
        if (request.medio() != null) {
            switch (request.medio().toUpperCase()) {
                case "SMS":
                    notificadorService.setEstrategia(new NotificacionSMS());
                    break;
                case "WHATSAPP":
                    notificadorService.setEstrategia(new NotificacionWhatsApp());
                    break;
                default:
                    notificadorService.setEstrategia(new NotificacionEmail());
                    break;
            }
        } else {
            notificadorService.setEstrategia(new NotificacionEmail());
        }

        notificadorService.enviarNotificacion(request.destinatario(), request.mensaje());
        return ResponseEntity.ok().build();
    }
}
