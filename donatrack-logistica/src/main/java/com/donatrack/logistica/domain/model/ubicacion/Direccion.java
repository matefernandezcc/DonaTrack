package com.donatrack.logistica.domain.model.ubicacion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Direccion {
  private String calle;
  private double altura;
  private String localidad;
  private Provincia provincia;
  private String codigoPostal;
  private Coordenada coordenadas;

  public Direccion(
      String calle,
      double altura,
      String localidad,
      Provincia provincia,
      String codigoPostal,
      Coordenada coordenadas) {
    this.calle = calle;
    this.altura = altura;
    this.localidad = localidad;
    this.provincia = provincia;
    this.codigoPostal = codigoPostal;
    this.coordenadas = coordenadas;
  }
}
