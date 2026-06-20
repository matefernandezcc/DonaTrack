package com.donatrack.donaciones.domain.model.roles;

import com.donatrack.donaciones.domain.model.persona.PersonaJuridica;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Representante extends Rol {
  private String cargo;
  private PersonaJuridica organizacion;

  public Representante(String cargo, PersonaJuridica organizacion) {
    this.cargo = cargo;
    this.organizacion = organizacion;
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
