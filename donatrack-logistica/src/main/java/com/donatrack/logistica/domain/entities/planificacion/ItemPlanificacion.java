package com.donatrack.logistica.domain.entities.planificacion;

import java.util.UUID;

import com.donatrack.logistica.domain.entities.reparto.Direccion;

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
