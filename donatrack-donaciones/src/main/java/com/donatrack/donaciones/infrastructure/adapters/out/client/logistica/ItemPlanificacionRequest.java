package com.donatrack.donaciones.infrastructure.adapters.out.client.logistica;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPlanificacionRequest {
  private UUID idDonacion;
  private double peso;
  private double volumen;
  private String calleDestino;
  private String alturaDestino;
  private String localidadDestino;
}
