package com.donatrack.donaciones.application.ports.out;

import com.donatrack.donaciones.domain.entities.enums.MedioContacto;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificacionOutDTO {
  private UUID id;
  private String mensaje;
  private LocalDate fechaEnvio;
  private MedioContacto medio;
  private boolean leida;

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
