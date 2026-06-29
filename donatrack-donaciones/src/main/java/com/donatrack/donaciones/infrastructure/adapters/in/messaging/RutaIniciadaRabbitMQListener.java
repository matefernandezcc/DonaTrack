package com.donatrack.donaciones.infrastructure.adapters.in.messaging;

import com.donatrack.common.events.RutaIniciadaEvent;
import com.donatrack.donaciones.application.usecases.ProcesarInicioRutaUseCase;
import com.donatrack.donaciones.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RutaIniciadaRabbitMQListener {

    private final ProcesarInicioRutaUseCase procesarInicioRutaUseCase;

    public RutaIniciadaRabbitMQListener(ProcesarInicioRutaUseCase procesarInicioRutaUseCase) {
        this.procesarInicioRutaUseCase = procesarInicioRutaUseCase;
    }

    @RabbitListener(queues = RabbitMQConfig.RUTA_INICIADA_QUEUE)
    public void handleRutaIniciada(RutaIniciadaEvent event) {
        log.info("Recibido evento desde RabbitMQ RutaIniciadaEvent: {}", event);
        try {
            procesarInicioRutaUseCase.procesar(
                event.getRutaId(), 
                event.getPatenteCamion(), 
                event.getNombreChofer(), 
                event.getIdsDonaciones()
            );
            log.info("Procesamiento de RutaIniciadaEvent exitoso.");
        } catch (Exception e) {
            log.error("Error procesando RutaIniciadaEvent", e);
        }
    }
}
