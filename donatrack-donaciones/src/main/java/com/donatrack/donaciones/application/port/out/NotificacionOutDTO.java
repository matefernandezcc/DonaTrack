package com.donatrack.donaciones.application.port.out;

import java.time.LocalDate;
import java.util.UUID;

import com.donatrack.donaciones.domain.model.enums.MedioContacto;

import lombok.Getter;
import lombok.Setter;

public class NotificacionOutDTO {
  @Getter @Setter private UUID id;
  @Getter @Setter private String mensaje;
  @Getter @Setter private LocalDate fechaEnvio;
  @Getter @Setter private MedioContacto medio;
  @Getter @Setter private boolean leida;

  public NotificacionOutDTO(String mensaje, MedioContacto medio) {
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
