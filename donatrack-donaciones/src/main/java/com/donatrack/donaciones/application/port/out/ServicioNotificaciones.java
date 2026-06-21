package com.donatrack.donaciones.application.port.out;

import com.donatrack.donaciones.domain.entities.persona.Contacto;

public interface ServicioNotificaciones {
  void enviar(NotificacionOutDTO notificacion, Contacto contactoDestino);
}
