package com.donatrack.donaciones.infrastructure.adapters.in.api.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaJuridicaDTO extends PersonaDTO {
  private String razonSocial;
  private String tipo;
  private String rubro;
}
