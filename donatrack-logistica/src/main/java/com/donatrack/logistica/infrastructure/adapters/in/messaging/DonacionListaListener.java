package com.donatrack.logistica.infrastructure.adapters.in.messaging;

import com.donatrack.logistica.application.ports.in.RecepcionarDonacionListaPort;
import com.donatrack.logistica.domain.entities.Direccion;
import com.donatrack.logistica.domain.entities.ItemPlanificacion;
import com.donatrack.logistica.infrastructure.adapters.in.messaging.dtos.MensajeDonacionListaDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DonacionListaListener {

    private final RecepcionarDonacionListaPort recepcionarDonacionListaPort;

    public DonacionListaListener(RecepcionarDonacionListaPort recepcionarDonacionListaPort) {
        this.recepcionarDonacionListaPort = recepcionarDonacionListaPort;
    }

    // Spring ejecuta este método automáticamente cuando llega un mensaje
    // (declarando la cola si no existe)
    @RabbitListener(queuesToDeclare = @Queue("donaciones_listas_queue"))
    public void procesarMensaje(MensajeDonacionListaDTO mensaje) {

        // 1. Mapeamos el DTO sucio de Rabbit a nuestro objeto puro de Dominio
        ItemPlanificacion item = new ItemPlanificacion(
                mensaje.idDonacion(),
                mensaje.peso(),
                mensaje.volumen(),
                new Direccion(mensaje.calleDestino(), mensaje.alturaDestino(), mensaje.localidadDestino()));

        // 2. Ejecutamos el caso de uso enviando al Port IN
        recepcionarDonacionListaPort.recepcionar(item);

        log.info("✅ Ítem recepcionado y encolado para planificar a las 2 AM: {}", item.getIdDonacionOriginal());
    }
}