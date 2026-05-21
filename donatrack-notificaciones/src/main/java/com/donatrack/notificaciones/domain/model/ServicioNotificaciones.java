package com.donatrack.notificaciones.domain.model;
import com.donatrack.donaciones.domain.model.Contacto;

public interface ServicioNotificaciones {
    void enviar(Notificacion notificacion, Contacto contactoDestino);
}