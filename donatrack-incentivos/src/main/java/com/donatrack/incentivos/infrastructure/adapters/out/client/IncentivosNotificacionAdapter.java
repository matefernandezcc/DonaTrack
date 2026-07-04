package com.donatrack.incentivos.infrastructure.adapters.out.client;

import com.donatrack.incentivos.application.ports.out.IncentivosNotificacionPort;
import com.donatrack.incentivos.application.ports.out.NotificacionRequest;
import org.springframework.stereotype.Component;

@Component
public class IncentivosNotificacionAdapter implements IncentivosNotificacionPort {

    private final NotificacionClient notificacionClient;

    public IncentivosNotificacionAdapter(NotificacionClient notificacionClient) {
        this.notificacionClient = notificacionClient;
    }

    @Override
    public void enviarNotificacion(NotificacionRequest request) {
        notificacionClient.enviarNotificacion(request);
    }
}
