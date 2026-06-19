package com.donatrack.notificaciones.domain.model;

public class NotificacionFactory {
    
    public static Notificacion crearNotificacion(Evento evento, String destinatario, String mensaje) {
        return new Notificacion(destinatario, mensaje, evento, null);
    }

    public static Notificacion crearNotificacion(Evento evento, String destinatario, String mensaje, String medio) {
        return new Notificacion(destinatario, mensaje, evento, medio);
    }
}
