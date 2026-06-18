package com.donatrack.notificaciones.infrastructure.out.messaging;

import com.donatrack.notificaciones.application.port.out.EstrategiaNotificacion;

import com.donatrack.notificaciones.domain.model.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component("EMAIL")
public class NotificacionEmail implements EstrategiaNotificacion {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionEmail.class);

    @Override
    public void enviar(Notificacion notificacion) {
        // Simulación de envío por Email
        logger.info("Enviando Email a {}: {}", notificacion.getDestinatario(), notificacion.getMensaje());
        notificacion.setCompletada(true);
    }
}
