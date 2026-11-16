package com.donatrack.donaciones.infrastructure.adapters.in.api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaJuridicaDTO extends PersonaDTO {
  private String razonSocial;
  private String tipo;

  @JsonAlias({"tipoEntidad", "tipoPersonaJuridica"})
  private String tipoJuridica;

  private String rubro;
}
