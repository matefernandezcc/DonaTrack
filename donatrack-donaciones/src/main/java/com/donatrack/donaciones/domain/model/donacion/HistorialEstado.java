package com.donatrack.donaciones.domain.model.donacion;

import com.donatrack.donaciones.domain.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.model.roles.Administrador;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

public class HistorialEstado {
  @Getter @Setter private EstadoDonacion estado;
  @Getter @Setter private LocalDate fecha;
  @Getter @Setter private String observacion;
  @Getter @Setter private Administrador usuario;

  public HistorialEstado(EstadoDonacion estado, String observacion, Administrador usuario) {
    this.estado = estado;
    this.fecha = LocalDate.now(); // Se setea el día de hoy automáticamente
    this.observacion = observacion;
    this.usuario = usuario;
  }
}
