package com.donatrack.notificaciones.domain.service;

import com.donatrack.notificaciones.domain.model.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component("WHATSAPP")
public class NotificacionWhatsApp implements EstrategiaNotificacion {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionWhatsApp.class);

    @Override
    public void enviar(Notificacion notificacion) {
        // Simulación de envío por WhatsApp
        logger.info("Enviando WhatsApp al {}: {}", notificacion.getDestinatario(), notificacion.getMensaje());
        notificacion.setCompletada(true);
    }
}
