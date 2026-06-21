package com.donatrack.logistica.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Camion {
  private String patente;
  private Double capacidadVolumen;
  private Double altura;
  private Double capacidadCarga;
}
