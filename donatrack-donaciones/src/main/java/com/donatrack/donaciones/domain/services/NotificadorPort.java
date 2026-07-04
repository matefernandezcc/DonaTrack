package com.donatrack.donaciones.domain.services;

import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.enums.MedioContacto;

public interface NotificadorPort {
    void notificar(Contacto destinatario, String mensaje, MedioContacto medio);
}
