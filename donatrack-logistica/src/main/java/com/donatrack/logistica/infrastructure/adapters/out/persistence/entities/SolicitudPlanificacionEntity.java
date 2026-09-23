package com.donatrack.logistica.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "solicitudes_planificacion", schema = "logistica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudPlanificacionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "solicitud_planificacion_id")
  private UUID id;

  @Column(name = "fecha_solicitud", nullable = false)
  private LocalDateTime fechaSolicitud;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", length = 50, nullable = false)
  private EstadoPlanificacionEnum estado;

  @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ItemPlanificacionEntity> items = new ArrayList<>();

  @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL)
  private List<RutaDeRepartoEntity> rutasGeneradas = new ArrayList<>();

  public enum EstadoPlanificacionEnum {
    PENDIENTE,
    RECIBIDA,
    PROCESADA
  }
}
