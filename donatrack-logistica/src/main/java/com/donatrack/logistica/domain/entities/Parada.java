package com.donatrack.logistica.domain.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Parada {
  private Integer orden;
  private Direccion direccion;
  private Coordenada coordenada;
  private List<Entrega> entregas;
}
