package com.donatrack.donaciones.domain.model;

import com.donatrack.donaciones.domain.model.persona.Contacto;

public interface ServicioNotificaciones {
  void enviar(Notificacion notificacion, Contacto contactoDestino);
}
