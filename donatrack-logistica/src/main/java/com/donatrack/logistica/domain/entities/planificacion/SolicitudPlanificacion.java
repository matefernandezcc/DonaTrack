package com.donatrack.logistica.domain.entities.planificacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudPlanificacion {
  private UUID id;
  private LocalDateTime fechaSolicitud;
  private EstadoPlanificacion estado;
  private List<UUID> idsDonaciones;
  private List<RutaDeReparto> rutasGeneradas;

  public void procesarCallback(List<RutaDeReparto> rutas) {
    this.rutasGeneradas = rutas;
    this.estado = EstadoPlanificacion.PROCESADA;
  }
}
