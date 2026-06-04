package com.donatrack.notificaciones.domain.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Notificacion {
    private String destinatario; // Email, number, etc.
    private String mensaje;
    private LocalDateTime fechaEnvio;
    private boolean completada;

    public Notificacion(String destinatario, String mensaje) {
        this.destinatario = destinatario;
        this.mensaje = mensaje;
        this.fechaEnvio = LocalDateTime.now();
        this.completada = false;
    }
}
