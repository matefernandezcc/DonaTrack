package com.donatrack.logistica.domain.entities;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemPlanificacion {
  private UUID idDonacionOriginal;
  private Double pesoEstimado;
  private Double volumenEstimado;
  private Direccion destino;
}
