package com.donatrack.notificaciones.infrastructure.adapters.in.messaging;

import com.donatrack.common.events.NotificacionEntregaExitosaEvent;
import com.donatrack.common.events.NotificacionInicioRutaEvent;
import com.donatrack.notificaciones.application.usecases.NotificadorService;
import com.donatrack.notificaciones.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class NotificacionEntregaRabbitMQListener {

    private final NotificadorService notificadorService;

    public NotificacionEntregaRabbitMQListener(NotificadorService notificadorService) {
        this.notificadorService = notificadorService;
    }

    @RabbitListener(queues = RabbitMQConfig.ENTREGA_EXITOSA_NOTIF_QUEUE)
    public void handleNotificacionEntregaExitosa(NotificacionEntregaExitosaEvent event) {
        log.info("Recibido evento enriquecido desde RabbitMQ NotificacionEntregaExitosaEvent: {}", event);

        String fechaFormateada = event.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String horaFormateada = event.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm"));

        String baseMensaje = String.format(
            "¡Tu donación ha sido entregada con éxito! \nComprobante de Entrega:\n- Fecha: %s\n- Hora: %s\n- Camión: %s\n- Fotos de recepción: %s",
            fechaFormateada, horaFormateada, event.getPatenteCamion(), String.join(", ", event.getEnlacesFotos())
        );

        enviarNotificaciones(event.getContactosDonantes(), baseMensaje);
        enviarNotificaciones(event.getContactosEntidades(), baseMensaje);
    }

    private void enviarNotificaciones(List<NotificacionInicioRutaEvent.ContactoInfo> contactos, String mensaje) {
        if (contactos == null) return;

        for (NotificacionInicioRutaEvent.ContactoInfo contacto : contactos) {
            try {
                notificadorService.enviarNotificacion(contacto.getDestinatario(), mensaje, contacto.getMedio());
                log.info("Notificación enviada a {} vía {}", contacto.getDestinatario(), contacto.getMedio());
            } catch (Exception e) {
                log.error("Fallo al enviar notificación a {}", contacto.getDestinatario(), e);
            }
        }
    }
}
