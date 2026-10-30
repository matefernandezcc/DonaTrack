package com.donatrack.logistica.infrastructure.adapters.out.persistence.documents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DireccionDocument {
  private String calle;
  private String altura;
  private String localidad;
}
