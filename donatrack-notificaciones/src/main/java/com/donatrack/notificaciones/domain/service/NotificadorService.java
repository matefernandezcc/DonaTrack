package com.donatrack.notificaciones.domain.service;

import com.donatrack.notificaciones.domain.model.Notificacion;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificadorService {

    private final Map<String, EstrategiaNotificacion> estrategias;

    public NotificadorService(Map<String, EstrategiaNotificacion> estrategias) {
        this.estrategias = estrategias;
    }

    public void enviarNotificacion(String destinatario, String mensaje, String medio) {
        Notificacion notificacion = new Notificacion(destinatario, mensaje);
        EstrategiaNotificacion estrategia = estrategias.getOrDefault(
            medio != null ? medio.toUpperCase() : "EMAIL", 
            estrategias.get("EMAIL")
        );
        
        if (estrategia != null) {
            estrategia.enviar(notificacion);
        } else {
            // Fallback en caso de que EMAIL tampoco exista
            System.err.println("No se encontró estrategia de notificación para: " + medio);
        }
    }
}
