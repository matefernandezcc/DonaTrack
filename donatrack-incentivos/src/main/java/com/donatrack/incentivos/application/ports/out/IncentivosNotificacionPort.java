package com.donatrack.incentivos.application.ports.out;

public interface IncentivosNotificacionPort {
    void enviarNotificacion(NotificacionRequest request);
}
