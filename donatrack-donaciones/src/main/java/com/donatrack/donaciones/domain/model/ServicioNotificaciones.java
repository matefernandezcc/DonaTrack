package com.donatrack.donaciones.domain.model;

public interface ServicioNotificaciones {
  void enviar(Notificacion notificacion, Contacto contactoDestino);
}
