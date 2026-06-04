package com.donatrack.notificaciones.domain.service;

import com.donatrack.notificaciones.domain.model.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificacionSMS implements EstrategiaNotificacion {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionSMS.class);

    @Override
    public void enviar(Notificacion notificacion) {
        // Simulación de envío por SMS
        logger.info("Enviando SMS al {}: {}", notificacion.getDestinatario(), notificacion.getMensaje());
        notificacion.setCompletada(true);
    }
}
