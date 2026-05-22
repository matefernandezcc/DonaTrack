package com.donatrack.donaciones.domain.model;

import lombok.Getter;
import lombok.Setter;

public class Representante extends Rol {
  @Getter @Setter private String cargo;
  @Getter @Setter private PersonaJuridica Organizacion;

  public Representante(String cargo, PersonaJuridica organizacion) {
    this.cargo = cargo;
    this.Organizacion = organizacion;
  }
}
