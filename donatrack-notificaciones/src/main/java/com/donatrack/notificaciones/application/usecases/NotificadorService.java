package com.donatrack.notificaciones.application.usecases;

import com.donatrack.notificaciones.application.ports.out.NotificacionAdapter;
import com.donatrack.notificaciones.domain.entities.Notificacion;
import com.donatrack.notificaciones.infrastructure.adapters.out.persistence.entities.EventoNotificacionEntity;
import com.donatrack.notificaciones.infrastructure.adapters.out.persistence.entities.NotificacionEntity;
import com.donatrack.notificaciones.infrastructure.adapters.out.persistence.repositories.EventoNotificacionJpaRepository;
import com.donatrack.notificaciones.infrastructure.adapters.out.persistence.repositories.NotificacionJpaRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificadorService {

  private final Map<String, NotificacionAdapter> adaptadores;
  private final EventoNotificacionJpaRepository eventoRepository;
  private final NotificacionJpaRepository notificacionRepository;

  public NotificadorService(Map<String, NotificacionAdapter> adaptadores) {
    this(adaptadores, null, null);
  }

  @Autowired
  public NotificadorService(
      Map<String, NotificacionAdapter> adaptadores,
      EventoNotificacionJpaRepository eventoRepository,
      NotificacionJpaRepository notificacionRepository) {
    this.adaptadores = adaptadores;
    this.eventoRepository = eventoRepository;
    this.notificacionRepository = notificacionRepository;
  }

  public void enviarNotificacion(String destinatario, String mensaje, String medio) {
    enviarNotificacion(destinatario, mensaje, medio, "NOTIFICACION_API");
  }

  public void enviarNotificacion(
      String destinatario, String mensaje, String medio, String tipoEvento) {
    Notificacion notificacion = new Notificacion(destinatario, mensaje);
    String clave = medio != null ? medio.trim().toUpperCase() : "EMAIL";
    if ("CORREO".equals(clave)) {
      clave = "EMAIL";
    } else if ("TELEFONO".equals(clave)) {
      clave = "SMS";
    }

    NotificacionAdapter adaptador = adaptadores.getOrDefault(clave, adaptadores.get("EMAIL"));

    boolean enviada = false;
    if (adaptador != null) {
      adaptador.enviar(notificacion);
      enviada = true;
    } else {
      System.err.println("No se encontró adaptador de notificación para: " + medio);
    }

    persistirNotificacion(destinatario, mensaje, clave, tipoEvento, enviada);
  }

  private void persistirNotificacion(
      String destinatario, String mensaje, String medio, String tipoEvento, boolean completada) {
    if (eventoRepository == null || notificacionRepository == null) {
      return;
    }
    try {
      EventoNotificacionEntity eventoEntity = new EventoNotificacionEntity();
      eventoEntity.setTipoEvento(tipoEvento != null ? tipoEvento : "NOTIFICACION_API");
      String desc =
          mensaje != null && mensaje.length() > 240 ? mensaje.substring(0, 240) : mensaje;
      eventoEntity.setDescripcion(desc);
      eventoEntity.setFechaRecepcion(LocalDateTime.now());
      EventoNotificacionEntity savedEvento = eventoRepository.save(eventoEntity);

      NotificacionEntity notifEntity = new NotificacionEntity();
      notifEntity.setEventoNotificacion(savedEvento);
      notifEntity.setDestinatario(destinatario);
      notifEntity.setMedio(medio);
      notifEntity.setMensaje(mensaje);
      notifEntity.setFechaEnvio(LocalDateTime.now());
      notifEntity.setCompletada(completada);
      notificacionRepository.save(notifEntity);
    } catch (Exception e) {
      System.err.println("No se pudo persistir la notificación en base de datos: " + e.getMessage());
    }
  }
}
