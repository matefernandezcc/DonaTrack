package com.donatrack.logistica.infrastructure.adapters.out.messaging;

import com.donatrack.common.events.RutaIniciadaEvent;
import com.donatrack.common.events.EntregaRealizadaEvent;
import com.donatrack.common.events.EntregaNoSatisfactoriaEvent;
import com.donatrack.logistica.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LogisticaRabbitMQAdapter {

    private final RabbitTemplate rabbitTemplate;

    public LogisticaRabbitMQAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @EventListener
    public void handleRutaIniciadaEvent(RutaIniciadaEvent event) {
        log.info("Recibido evento local RutaIniciadaEvent para rutaId: {}. Publicando en RabbitMQ...", event.getRutaId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.LOGISTICA_EXCHANGE, "ruta.iniciada", event);
        log.info("Evento RutaIniciadaEvent publicado exitosamente en RabbitMQ.");
    }

    @EventListener
    public void handleEntregaRealizadaEvent(EntregaRealizadaEvent event) {
        log.info("Recibido evento local EntregaRealizadaEvent para donacionId: {}. Publicando en RabbitMQ...", event.getIdDonacion());
        rabbitTemplate.convertAndSend(RabbitMQConfig.LOGISTICA_EXCHANGE, "entrega.realizada", event);
        log.info("Evento EntregaRealizadaEvent publicado exitosamente en RabbitMQ.");
    }

    @EventListener
    public void handleEntregaNoSatisfactoriaEvent(EntregaNoSatisfactoriaEvent event) {
        log.info("Recibido evento local EntregaNoSatisfactoriaEvent para donacionId: {}. Publicando en RabbitMQ...", event.getIdDonacion());
        rabbitTemplate.convertAndSend(RabbitMQConfig.LOGISTICA_EXCHANGE, "entrega.fallida", event);
        log.info("Evento EntregaNoSatisfactoriaEvent publicado exitosamente en RabbitMQ.");
    }
}
