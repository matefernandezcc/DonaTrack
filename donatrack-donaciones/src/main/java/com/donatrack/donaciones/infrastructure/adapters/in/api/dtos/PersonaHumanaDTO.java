package com.donatrack.donaciones.infrastructure.adapters.in.api.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaHumanaDTO extends PersonaDTO {
  private String nombre;
  private String apellido;
  private int edad;
}
