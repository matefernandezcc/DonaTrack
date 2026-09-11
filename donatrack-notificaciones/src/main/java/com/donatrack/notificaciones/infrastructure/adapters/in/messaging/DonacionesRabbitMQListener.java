package com.donatrack.notificaciones.infrastructure.adapters.in.messaging;

import com.donatrack.notificaciones.application.usecases.NotificadorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DonacionesRabbitMQListener {

    private final NotificadorService notificadorService;

    public DonacionesRabbitMQListener(NotificadorService notificadorService) {
        this.notificadorService = notificadorService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "notificacion_general_queue", durable = "true"),
            exchange = @Exchange(value = "donaciones.exchange", type = "topic"),
            key = "notificacion.general"
    ))
    public void onNotificacionGeneral(NotificacionRequestDTO request) {
        log.info("Recibido evento de notificación general (Donaciones) para: {}", request.destinatario);

        notificadorService.enviarNotificacion(
                request.destinatario,
                request.mensaje,
                request.medio != null ? request.medio : "EMAIL"
        );
    }

    public static class NotificacionRequestDTO {
        public String destinatario;
        public String asunto;
        public String mensaje;
        public String medio;
        
        public NotificacionRequestDTO() {}
    }
}
