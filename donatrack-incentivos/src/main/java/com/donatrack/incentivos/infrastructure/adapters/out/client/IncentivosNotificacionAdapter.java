package com.donatrack.incentivos.infrastructure.adapters.out.client;

import com.donatrack.incentivos.application.ports.out.IncentivosNotificacionPort;
import com.donatrack.incentivos.application.ports.out.NotificacionRequest;
import com.donatrack.incentivos.infrastructure.config.RabbitMQIncentivosConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class IncentivosNotificacionAdapter implements IncentivosNotificacionPort {

    private final RabbitTemplate rabbitTemplate;

    public IncentivosNotificacionAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void enviarNotificacion(NotificacionRequest request) {
        // Enviar a RabbitMQ en vez de llamada sincrónica Feign
        rabbitTemplate.convertAndSend(
                RabbitMQIncentivosConfig.INCENTIVOS_EXCHANGE,
                "insignia.nueva",
                request
        );
    }
}
