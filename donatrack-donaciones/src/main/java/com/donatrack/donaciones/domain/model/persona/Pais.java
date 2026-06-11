package com.donatrack.donaciones.domain.model.persona;

import lombok.Getter;
import lombok.Setter;

public class Pais {
  @Getter @Setter private String nombrePais;
  @Getter @Setter private String nacionalidad;

  public Pais(String nombrePais, String nacionalidad) {
    this.nombrePais = nombrePais;
    this.nacionalidad = nacionalidad;
  }
}
