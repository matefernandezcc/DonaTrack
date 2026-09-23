package com.donatrack.donaciones.infrastructure.adapters.out.client;

import com.donatrack.donaciones.application.ports.out.NotificacionOutDTO;
import com.donatrack.donaciones.application.ports.out.ServicioNotificaciones;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificacionesMessagingAdapter implements ServicioNotificaciones {

    private final RabbitTemplate rabbitTemplate;

    public NotificacionesMessagingAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void enviar(NotificacionOutDTO notificacion, Contacto contactoDestino) {
        String destinatario = "";
        String medio = contactoDestino.getMedioPredeterminado().name();

        switch (contactoDestino.getMedioPredeterminado()) {
            case WHATSAPP:
                destinatario = contactoDestino.getWhatsapp();
                break;
            case TELEFONO:
                destinatario = contactoDestino.getTelefono();
                break;
            case CORREO:
            default:
                destinatario = contactoDestino.getCorreoElectronico();
                break;
        }

        NotificacionRequest request = new NotificacionRequest(
            destinatario,
            notificacion.getMensaje(),
            medio
        );
        
        try {
            // Using a default exchange for donaciones internal events
            rabbitTemplate.convertAndSend(
                "donaciones.exchange",
                "notificacion.general",
                request
            );
        } catch (Exception e) {
            System.err.println("Error al enviar notificación a RabbitMQ: " + e.getMessage());
        }
    }
}
