package com.donatrack.donaciones.domain.services;

import com.donatrack.donaciones.domain.entities.enums.MedioContacto;
import com.donatrack.donaciones.domain.entities.persona.Contacto;

public interface NotificadorPort {
  void notificar(Contacto destinatario, String mensaje, MedioContacto medio);
}
