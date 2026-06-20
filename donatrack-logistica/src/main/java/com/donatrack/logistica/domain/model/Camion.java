package com.donatrack.logistica.domain.model;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Camion {
  private String patente;
  String ubicacionActual;
  private LocalDate ultimaActualizacion;

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
