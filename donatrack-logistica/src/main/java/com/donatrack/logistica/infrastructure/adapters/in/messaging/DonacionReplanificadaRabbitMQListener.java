package com.donatrack.logistica.infrastructure.adapters.in.messaging;

import com.donatrack.common.events.DonacionReplanificadaEvent;
import com.donatrack.logistica.application.ports.out.EntregaRepositoryPort;
import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Component
@Slf4j
public class DonacionReplanificadaRabbitMQListener {

    private final EntregaRepositoryPort entregaRepository;

    public DonacionReplanificadaRabbitMQListener(EntregaRepositoryPort entregaRepository) {
        this.entregaRepository = entregaRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.REPLANIFICADA_QUEUE)
    public void handleDonacionReplanificada(DonacionReplanificadaEvent event) {
        log.info("Recibido evento desde RabbitMQ DonacionReplanificadaEvent para donacionId: {}", event.getIdDonacion());
        try {
            Optional<Entrega> entregaOpt = entregaRepository.buscarPorId(event.getIdDonacion());
            if (entregaOpt.isPresent()) {
                Entrega entrega = entregaOpt.get();
                entrega.volverAPendiente();
                entregaRepository.guardar(entrega);
                log.info("La entrega ha vuelto al estado PENDIENTE correctamente.");
            } else {
                log.warn("No se encontró la entrega correspondiente a la donación para replanificar.");
            }
        } catch (Exception e) {
            log.error("Error procesando DonacionReplanificadaEvent", e);
        }
    }
}
