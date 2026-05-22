package com.donatrack.donaciones.domain.model;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

public class Camion {
  @Getter @Setter private String patente;
  @Getter String ubicacionActual;
  @Getter @Setter private LocalDate ultimaActualizacion;

  public Camion(String patente, String ubicacionActual) {
    this.patente = patente;
    this.ubicacionActual = ubicacionActual;
    this.ultimaActualizacion = LocalDate.now();
  }

  public void setUbicacionActual(String ubicacionActual) {
    this.ubicacionActual = ubicacionActual;
    this.ultimaActualizacion = LocalDate.now();
  }
}
