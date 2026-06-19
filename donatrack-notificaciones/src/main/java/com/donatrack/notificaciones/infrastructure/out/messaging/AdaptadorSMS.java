package com.donatrack.notificaciones.infrastructure.out.messaging;

import com.donatrack.notificaciones.domain.port.out.NotificacionAdapter;
import com.donatrack.notificaciones.domain.model.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("SMS")
public class AdaptadorSMS implements NotificacionAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AdaptadorSMS.class);

    @Override
    public void enviar(Notificacion notificacion) {
        // Simulación de envío por SMS
        logger.info("Enviando SMS al {}: {}", notificacion.getDestinatario(), notificacion.getMensaje());
        notificacion.setCompletada(true);
    }
}
