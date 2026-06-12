package com.donatrack.donaciones.infrastructure.adapters.out.client;

import com.donatrack.donaciones.domain.model.Notificacion;
import com.donatrack.donaciones.domain.model.ServicioNotificaciones;
import com.donatrack.donaciones.domain.model.persona.Contacto;
import org.springframework.stereotype.Service;

@Service
public class NotificacionesFeignAdapter implements ServicioNotificaciones {

    private final NotificacionClient notificacionClient;

    public NotificacionesFeignAdapter(NotificacionClient notificacionClient) {
        this.notificacionClient = notificacionClient;
    }

    @Override
    public void enviar(Notificacion notificacion, Contacto contactoDestino) {
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
            notificacionClient.enviarNotificacion(request);
        } catch (Exception e) {
            // Log de error temporal
            System.err.println("Error al enviar notificación: " + e.getMessage());
        }
    }
}
