package com.donatrack.notificaciones.infrastructure.adapters.out.messaging;

import com.donatrack.notificaciones.application.ports.out.NotificacionAdapter;
import com.donatrack.notificaciones.domain.entities.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("EMAIL")
public class AdaptadorEmail implements NotificacionAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AdaptadorEmail.class);

    @Override
    public void enviar(Notificacion notificacion) {
        // Simulación de envío por Email
        logger.info("Enviando Email a {}: {}", notificacion.getDestinatario(), notificacion.getMensaje());
        notificacion.setCompletada(true);
    }
}
