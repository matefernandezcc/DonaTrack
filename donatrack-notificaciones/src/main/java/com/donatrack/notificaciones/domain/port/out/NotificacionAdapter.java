package com.donatrack.notificaciones.domain.port.out;

import com.donatrack.notificaciones.domain.entities.Notificacion;

public interface NotificacionAdapter {
    /**
     * Envía la notificación y marca como completada si el envío (simulado) fue exitoso.
     */
    void enviar(Notificacion notificacion);
}
