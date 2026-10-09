package com.donatrack.notificaciones.domain.entities;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Notificacion {
  private String destinatario; // Email, number, etc.
  private String mensaje;
  private LocalDateTime fechaEnvio;
  private boolean completada;
  private Evento evento;
  private String medio;

  public Notificacion(String destinatario, String mensaje) {
    this.destinatario = destinatario;
    this.mensaje = mensaje;
    this.fechaEnvio = LocalDateTime.now();
    this.completada = false;
  }

  public Notificacion(String destinatario, String mensaje, Evento evento, String medio) {
    this(destinatario, mensaje);
    this.evento = evento;
    this.medio = medio;
  }

  public void marcarComoCompletada() {
    this.completada = true;
  }
}
