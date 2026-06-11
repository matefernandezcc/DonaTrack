package com.donatrack.donaciones.domain.model.roles;

import com.donatrack.donaciones.domain.model.persona.PersonaJuridica;

import lombok.Getter;
import lombok.Setter;

public class Representante extends Rol {
  @Getter @Setter private String cargo;
  @Getter @Setter private PersonaJuridica Organizacion;

  public Representante(String cargo, PersonaJuridica organizacion) {
    this.cargo = cargo;
    this.Organizacion = organizacion;
  }

  @Override
  public boolean esValidoParaHumana() { 
    return true; // Un representante debe ser una persona humana
  }

  @Override
  public boolean esValidoParaJuridica() { 
    return false; 
  }
}
