package com.donatrack.notificaciones.infrastructure.adapters.in.messaging;

import com.donatrack.common.events.NotificacionEntregaFallidaEvent;
import com.donatrack.common.events.NotificacionInicioRutaEvent;
import com.donatrack.notificaciones.application.usecases.NotificadorService;
import com.donatrack.notificaciones.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component
@Slf4j
public class NotificacionEntregaFallidaRabbitMQListener {

    private final NotificadorService notificadorService;

    public NotificacionEntregaFallidaRabbitMQListener(NotificadorService notificadorService) {
        this.notificadorService = notificadorService;
    }

    @RabbitListener(queues = RabbitMQConfig.ENTREGA_FALLIDA_NOTIF_QUEUE)
    public void handleNotificacionEntregaFallida(NotificacionEntregaFallidaEvent event) {
        log.info("Recibido evento enriquecido desde RabbitMQ NotificacionEntregaFallidaEvent: {}", event);

        String replanificacionMsg = event.isPuedeReplanificarse() 
            ? "La donación volverá al depósito y se generará una nueva asignación de ruta en breve." 
            : "Lamentablemente, la entrega no podrá ser replanificada.";

        String baseMensaje = String.format(
            "Lamentamos informarte que hubo un inconveniente con la entrega de la donación.\nMotivo: %s\n\n%s\n\nEl equipo revisará el caso a la brevedad.",
            event.getMotivo(), replanificacionMsg
        );

        enviarNotificaciones(event.getContactosDonantes(), baseMensaje);
        enviarNotificaciones(event.getContactosEntidades(), baseMensaje);
        enviarNotificaciones(event.getContactosAdministradores(), baseMensaje);
    }

    private void enviarNotificaciones(List<NotificacionInicioRutaEvent.ContactoInfo> contactos, String mensaje) {
        if (contactos == null) return;

        for (NotificacionInicioRutaEvent.ContactoInfo contacto : contactos) {
            try {
                notificadorService.enviarNotificacion(contacto.getDestinatario(), mensaje, contacto.getMedio());
                log.info("Notificación de fallo enviada a {} vía {} (Rol: {})", 
                         contacto.getDestinatario(), contacto.getMedio(), contacto.getRol());
            } catch (Exception e) {
                log.error("Error al enviar notificación a {}", contacto.getDestinatario(), e);
            }
        }
    }
}
