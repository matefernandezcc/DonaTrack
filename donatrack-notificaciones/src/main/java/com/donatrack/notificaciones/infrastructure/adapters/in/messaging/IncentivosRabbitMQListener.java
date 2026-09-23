package com.donatrack.notificaciones.infrastructure.adapters.in.messaging;

import com.donatrack.notificaciones.application.usecases.NotificadorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IncentivosRabbitMQListener {

    private final NotificadorService notificadorService;

    public IncentivosRabbitMQListener(NotificadorService notificadorService) {
        this.notificadorService = notificadorService;
    }

    @RabbitListener(bindings = @org.springframework.amqp.rabbit.annotation.QueueBinding(
            value = @Queue(value = "nueva_insignia_queue", durable = "true"),
            exchange = @org.springframework.amqp.rabbit.annotation.Exchange(value = "incentivos.exchange", type = "topic"),
            key = "insignia.nueva"
    ))
    public void onNuevaInsignia(NotificacionRequestDTO request) {
        log.info("Recibido evento de nueva insignia para: {}", request.destinatario);

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
        
        // Se pueden añadir constructores o dejar public fields para que Jackson deserialice.
        public NotificacionRequestDTO() {}
    }
}
