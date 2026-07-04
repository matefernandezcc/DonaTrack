package com.donatrack.donaciones.domain.entities.donacion;

import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistorialEstado {
  private EstadoDonacion estado;
  private LocalDate fecha;
  private String observacion;
  private String usuarioId;

  public HistorialEstado(EstadoDonacion estado, String observacion, String usuarioId) {
    this.estado = estado;
    this.fecha = LocalDate.now(); // Se setea el día de hoy automáticamente
    this.observacion = observacion;
    this.usuarioId = usuarioId;
  }
}
