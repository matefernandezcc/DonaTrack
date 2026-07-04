package com.donatrack.donaciones.infrastructure.adapters.in.messaging;

import com.donatrack.common.events.EntregaRealizadaEvent;
import com.donatrack.donaciones.application.usecases.ConfirmarRecepcionYNotificarUseCase;
import com.donatrack.donaciones.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EntregaRealizadaRabbitMQListener {

    private final ConfirmarRecepcionYNotificarUseCase confirmarRecepcionYNotificarUseCase;

    public EntregaRealizadaRabbitMQListener(ConfirmarRecepcionYNotificarUseCase confirmarRecepcionYNotificarUseCase) {
        this.confirmarRecepcionYNotificarUseCase = confirmarRecepcionYNotificarUseCase;
    }

    @RabbitListener(queues = RabbitMQConfig.ENTREGA_REALIZADA_QUEUE)
    public void handleEntregaRealizada(EntregaRealizadaEvent event) {
        log.info("Recibido evento desde RabbitMQ EntregaRealizadaEvent: {}", event);
        try {
            confirmarRecepcionYNotificarUseCase.procesar(event);
            log.info("Procesamiento de EntregaRealizadaEvent exitoso.");
        } catch (Exception e) {
            log.error("Error procesando EntregaRealizadaEvent", e);
        }
    }
}
