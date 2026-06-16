package com.donatrack.donaciones.application.port.out;

import com.donatrack.donaciones.domain.model.persona.Contacto;

public interface ServicioNotificaciones {
  void enviar(NotificacionOutDTO notificacion, Contacto contactoDestino);
}
