package com.donatrack.notificaciones.infrastructure.adapters.in.messaging;

import com.donatrack.common.events.NotificacionInicioRutaEvent;
import com.donatrack.notificaciones.application.usecases.NotificadorService;
import com.donatrack.notificaciones.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component
@Slf4j
public class NotificacionInicioRutaRabbitMQListener {

    private final NotificadorService notificadorService;

    public NotificacionInicioRutaRabbitMQListener(NotificadorService notificadorService) {
        this.notificadorService = notificadorService;
    }

    @RabbitListener(queues = RabbitMQConfig.RUTA_INICIADA_NOTIF_QUEUE)
    public void handleNotificacionInicioRuta(NotificacionInicioRutaEvent event) {
        log.info("Recibido evento enriquecido desde RabbitMQ NotificacionInicioRutaEvent: {}", event);

        String baseMensaje = String.format("¡Tu donación está en camino! El chofer %s inició la ruta %s en el camión %s. Sigue tu entrega en vivo aquí: %s",
                event.getNombreChofer(), event.getRutaId(), event.getPatenteCamion(), event.getLinkSeguimiento());

        enviarNotificaciones(event.getContactosDonantes(), baseMensaje);
        enviarNotificaciones(event.getContactosEntidades(), baseMensaje);
    }

    private void enviarNotificaciones(List<NotificacionInicioRutaEvent.ContactoInfo> contactos, String mensaje) {
        if (contactos == null) return;
        
        for (NotificacionInicioRutaEvent.ContactoInfo contacto : contactos) {
            try {
                notificadorService.enviarNotificacion(contacto.getDestinatario(), mensaje, contacto.getMedio());
                log.info("Notificación enviada a {} vía {}", contacto.getDestinatario(), contacto.getMedio());
            } catch (Exception e) {
                log.error("Fallo al enviar notificación a {}", contacto.getDestinatario(), e);
            }
        }
    }
}
