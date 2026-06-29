package com.donatrack.logistica.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "solicitudes_planificacion", schema = "logistica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudPlanificacionEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "fecha_solicitud", nullable = false)
  private LocalDateTime fechaSolicitud;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", length = 20, nullable = false)
  private EstadoPlanificacionEnum estado;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "ids_donaciones", columnDefinition = "uuid[]")
  private List<UUID> idsDonaciones;

  @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ItemPlanificacionEntity> items;

  @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL)
  private List<RutaDeRepartoEntity> rutasGeneradas;

  public enum EstadoPlanificacionEnum {
    PENDIENTE,
    RECIBIDA,
    PROCESADA
  }
}
