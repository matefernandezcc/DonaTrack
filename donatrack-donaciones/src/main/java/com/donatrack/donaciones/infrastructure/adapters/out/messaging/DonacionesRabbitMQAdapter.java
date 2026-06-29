package com.donatrack.donaciones.infrastructure.adapters.out.messaging;

import com.donatrack.common.events.NotificacionInicioRutaEvent;
import com.donatrack.donaciones.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DonacionesRabbitMQAdapter {

    private final RabbitTemplate rabbitTemplate;

    public DonacionesRabbitMQAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @EventListener
    public void handleNotificacionInicioRutaEvent(NotificacionInicioRutaEvent event) {
        log.info("Recibido evento local NotificacionInicioRutaEvent para rutaId: {}. Publicando en RabbitMQ...", event.getRutaId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.DONACIONES_EXCHANGE, "notificacion.inicio.ruta", event);
        log.info("Evento NotificacionInicioRutaEvent publicado exitosamente en RabbitMQ.");
    }
}
