package com.donatrack.notificaciones.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.notificaciones.domain.entities.Evento;
import com.donatrack.notificaciones.domain.entities.Notificacion;
import com.donatrack.notificaciones.domain.entities.TipoEvento;
import com.donatrack.notificaciones.infrastructure.adapters.out.persistence.entities.EventoNotificacionEntity;
import com.donatrack.notificaciones.infrastructure.adapters.out.persistence.entities.NotificacionEntity;

public class NotificacionMapper {

  public static NotificacionEntity toEntity(Notificacion domain) {
    if (domain == null) return null;

    NotificacionEntity entity = new NotificacionEntity();
    entity.setDestinatario(domain.getDestinatario());
    entity.setMensaje(domain.getMensaje());
    entity.setMedio(domain.getMedio());
    entity.setFechaEnvio(domain.getFechaEnvio());
    entity.setCompletada(domain.isCompletada());

    if (domain.getEvento() != null) {
      EventoNotificacionEntity ene = new EventoNotificacionEntity();
      if (domain.getEvento().getTipoEvento() != null) {
        ene.setTipoEvento(domain.getEvento().getTipoEvento().name());
      }
      ene.setDescripcion(domain.getEvento().getDescripcion());
      ene.setFechaRecepcion(domain.getFechaEnvio());
      entity.setEventoNotificacion(ene);
    }

    return entity;
  }

  public static Notificacion toDomain(NotificacionEntity entity) {
    if (entity == null) return null;

    Evento evento = null;
    if (entity.getEventoNotificacion() != null) {
      TipoEvento tipo = null;
      if (entity.getEventoNotificacion().getTipoEvento() != null) {
        try {
          tipo = TipoEvento.valueOf(entity.getEventoNotificacion().getTipoEvento());
        } catch (IllegalArgumentException ignored) {
        }
      }
      evento = new Evento(tipo, entity.getEventoNotificacion().getDescripcion(), null);
    }

    Notificacion domain =
        new Notificacion(entity.getDestinatario(), entity.getMensaje(), evento, entity.getMedio());
    domain.setFechaEnvio(entity.getFechaEnvio());
    if (entity.getCompletada() != null && entity.getCompletada()) {
      domain.marcarComoCompletada();
    }

    return domain;
  }
}
