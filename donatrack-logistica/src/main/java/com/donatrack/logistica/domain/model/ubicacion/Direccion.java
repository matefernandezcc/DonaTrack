package com.donatrack.logistica.domain.model.ubicacion;

import lombok.Getter;
import lombok.Setter;

public class Direccion {
  @Getter @Setter private String calle;
  @Getter @Setter private double altura;
  @Getter @Setter private String localidad;
  @Getter @Setter private Provincia provincia;
  @Getter @Setter private String codigoPostal;
  @Getter @Setter private Coordenada coordenadas;

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
