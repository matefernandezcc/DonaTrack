package com.donatrack.donaciones.application.ports.out;

import com.donatrack.donaciones.domain.entities.persona.Contacto;

public interface ServicioNotificaciones {
  void enviar(NotificacionOutDTO notificacion, Contacto contactoDestino);
}
