package com.donatrack.donaciones.domain.entities.persona.ubicacion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pais {
  private String nombrePais;
  private String nacionalidad;

  public Pais(String nombrePais, String nacionalidad) {
    this.nombrePais = nombrePais;
    this.nacionalidad = nacionalidad;
  }
}
