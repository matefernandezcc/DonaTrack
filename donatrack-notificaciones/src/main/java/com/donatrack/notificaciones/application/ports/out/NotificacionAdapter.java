package com.donatrack.notificaciones.application.ports.out;

import com.donatrack.notificaciones.domain.entities.Notificacion;

public interface NotificacionAdapter {
    /**
     * Envía la notificación y marca como completada si el envío (simulado) fue exitoso.
     */
    void enviar(Notificacion notificacion);
}
