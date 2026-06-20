package com.donatrack.donaciones.domain.model.persona.ubicacion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Provincia {
  private String nombreProvincia;
  private Pais pais;

  public Provincia(String nombreProvincia, Pais pais) {
    this.nombreProvincia = nombreProvincia;
    this.pais = pais;
  }
}
