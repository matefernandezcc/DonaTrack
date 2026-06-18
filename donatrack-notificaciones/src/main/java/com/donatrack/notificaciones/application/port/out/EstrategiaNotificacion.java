package com.donatrack.notificaciones.application.port.out;

import com.donatrack.notificaciones.domain.model.Notificacion;

public interface EstrategiaNotificacion {
    /**
     * Envía la notificación y marca como completada si el envío (simulado) fue exitoso.
     */
    void enviar(Notificacion notificacion);
}
