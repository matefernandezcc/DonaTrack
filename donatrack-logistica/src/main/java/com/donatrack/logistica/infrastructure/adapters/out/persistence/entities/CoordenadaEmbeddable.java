package com.donatrack.logistica.infrastructure.adapters.out.persistence.entities;

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
public class CoordenadaEmbeddable {

  @Column(name = "latitud")
  private Double latitud;

  @Column(name = "longitud")
  private Double longitud;
}
