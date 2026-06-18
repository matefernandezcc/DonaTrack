package com.donatrack.logistica.domain.model.ubicacion;

import lombok.Getter;
import lombok.Setter;

public class Provincia {
  @Getter @Setter private String nombreProvincia;
  @Getter @Setter private Pais pais;

  public Provincia(String nombreProvincia, Pais pais) {
    this.nombreProvincia = nombreProvincia;
    this.pais = pais;
  }
}
