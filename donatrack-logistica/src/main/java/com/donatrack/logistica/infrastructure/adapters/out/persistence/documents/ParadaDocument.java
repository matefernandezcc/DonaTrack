package com.donatrack.logistica.infrastructure.adapters.out.persistence.documents;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParadaDocument {
  private Integer orden;
  private DireccionDocument direccion;
  private CoordenadaDocument coordenada;
  private List<EntregaDocument> entregas;
}
