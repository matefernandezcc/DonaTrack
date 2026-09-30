package com.donatrack.donaciones.infrastructure.adapters.in.api.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DireccionDTO {
  private String calle;
  private Double altura;
  private String localidad;
  private ProvinciaDTO provincia;
  private String codigoPostal;
  private CoordenadaDTO coordenadas;
}
