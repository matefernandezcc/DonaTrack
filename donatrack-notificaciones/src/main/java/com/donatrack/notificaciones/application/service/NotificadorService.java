package com.donatrack.notificaciones.application.service;

import com.donatrack.notificaciones.domain.port.out.NotificacionAdapter;
import com.donatrack.notificaciones.domain.model.Notificacion;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificadorService {

    private final Map<String, NotificacionAdapter> adaptadores;

    public NotificadorService(Map<String, NotificacionAdapter> adaptadores) {
        this.adaptadores = adaptadores;
    }

    public void enviarNotificacion(String destinatario, String mensaje, String medio) {
        Notificacion notificacion = new Notificacion(destinatario, mensaje);
        NotificacionAdapter adaptador = adaptadores.getOrDefault(
            medio != null ? medio.toUpperCase() : "EMAIL", 
            adaptadores.get("EMAIL")
        );
        
        if (adaptador != null) {
            adaptador.enviar(notificacion);
        } else {
            // Fallback en caso de que EMAIL tampoco exista
            System.err.println("No se encontró adaptador de notificación para: " + medio);
        }
    }
}
