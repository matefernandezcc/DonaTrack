package com.donatrack.donaciones.infrastructure.adapters.in.messaging;

import com.donatrack.common.events.EntregaNoSatisfactoriaEvent;
import com.donatrack.donaciones.application.usecases.ProcesarFallaEntregaYNotificarUseCase;
import com.donatrack.donaciones.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EntregaFallidaRabbitMQListener {

    private final ProcesarFallaEntregaYNotificarUseCase useCase;

    public EntregaFallidaRabbitMQListener(ProcesarFallaEntregaYNotificarUseCase useCase) {
        this.useCase = useCase;
    }

    @RabbitListener(queues = RabbitMQConfig.ENTREGA_FALLIDA_QUEUE)
    public void handleEntregaFallida(EntregaNoSatisfactoriaEvent event) {
        log.info("Recibido evento desde RabbitMQ EntregaNoSatisfactoriaEvent: {}", event);
        try {
            useCase.procesar(event);
            log.info("Procesamiento de EntregaNoSatisfactoriaEvent exitoso.");
        } catch (Exception e) {
            log.error("Error procesando EntregaNoSatisfactoriaEvent", e);
        }
    }
}
