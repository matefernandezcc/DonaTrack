package com.donatrack.donaciones.infrastructure.adapters.in.api.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProvinciaDTO {
  private String nombreProvincia;
  private PaisDTO pais;
}
