package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DireccionEmbeddable {

  @Column(name = "direccion_calle")
  private String calle;

  @Column(name = "direccion_altura")
  private String altura;

  @Column(name = "direccion_localidad")
  private String localidad;

  @Column(name = "direccion_pais")
  private String pais;

  @Column(name = "direccion_provincia")
  private String provincia;
}
