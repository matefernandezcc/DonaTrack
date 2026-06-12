package com.donatrack.notificaciones.domain.service;

import com.donatrack.notificaciones.domain.model.Notificacion;
import org.springframework.stereotype.Service;

@Service
public class NotificadorService {

    private EstrategiaNotificacion estrategiaNotificacion;

    public NotificadorService() {
        this.estrategiaNotificacion = new NotificacionEmail(); // Default
    }

    public NotificadorService(EstrategiaNotificacion estrategiaInicial) {
        this.estrategiaNotificacion = estrategiaInicial;
    }

    public void setEstrategia(EstrategiaNotificacion estrategia) {
        this.estrategiaNotificacion = estrategia;
    }

    public void enviarNotificacion(String destinatario, String mensaje) {
        Notificacion notificacion = new Notificacion(destinatario, mensaje);
        estrategiaNotificacion.enviar(notificacion);
    }
}
