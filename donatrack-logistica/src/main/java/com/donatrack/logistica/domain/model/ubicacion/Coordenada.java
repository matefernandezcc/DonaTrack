package com.donatrack.logistica.domain.model.ubicacion;

import lombok.Getter;
import lombok.Setter;

public class Coordenada {
  @Getter @Setter private double latitud;
  @Getter @Setter private double longitud;

  public Coordenada(double latitud, double longitud) {
    this.latitud = latitud;
    this.longitud = longitud;
  }
}
