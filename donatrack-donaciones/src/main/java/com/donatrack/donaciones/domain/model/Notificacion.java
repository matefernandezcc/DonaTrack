package com.donatrack.donaciones.domain.model;

import com.donatrack.donaciones.domain.enums.MedioContacto;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public class Notificacion {
  @Getter @Setter private UUID id;
  @Getter @Setter private String mensaje;
  @Getter @Setter private LocalDate fechaEnvio;
  @Getter @Setter private MedioContacto medio;
  @Getter @Setter private boolean leida;

  public Notificacion(String mensaje, MedioContacto medio) {
    this.id = UUID.randomUUID();
    this.mensaje = mensaje;
    this.medio = medio;
    this.fechaEnvio = LocalDate.now();
    this.leida = false;
  }

  public void marcarComoLeida() {
    this.leida = true;
  }
}
