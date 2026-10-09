package com.donatrack.notificaciones.application.usecases;

import com.donatrack.notificaciones.application.ports.out.NotificacionAdapter;
import com.donatrack.notificaciones.domain.entities.Notificacion;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class NotificadorService {

  private final Map<String, NotificacionAdapter> adaptadores;

  public NotificadorService(Map<String, NotificacionAdapter> adaptadores) {
    this.adaptadores = adaptadores;
  }

  public void enviarNotificacion(String destinatario, String mensaje, String medio) {
    Notificacion notificacion = new Notificacion(destinatario, mensaje);
    String clave = medio != null ? medio.trim().toUpperCase() : "EMAIL";
    if ("CORREO".equals(clave)) {
      clave = "EMAIL";
    } else if ("TELEFONO".equals(clave)) {
      clave = "SMS";
    }

    NotificacionAdapter adaptador = adaptadores.getOrDefault(clave, adaptadores.get("EMAIL"));

    if (adaptador != null) {
      adaptador.enviar(notificacion);
    } else {
      // Fallback en caso de que EMAIL tampoco exista
      System.err.println("No se encontró adaptador de notificación para: " + medio);
    }
  }
}
