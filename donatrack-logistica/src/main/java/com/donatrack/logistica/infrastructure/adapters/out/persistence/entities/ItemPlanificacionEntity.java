package com.donatrack.logistica.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "items_planificacion", schema = "logistica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemPlanificacionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "solicitud_id")
  private SolicitudPlanificacionEntity solicitud;

  @Column(name = "id_donacion_original", nullable = false)
  private UUID idDonacionOriginal;

  @Column(name = "peso_estimado")
  private Double pesoEstimado;

  @Column(name = "volumen_estimado")
  private Double volumenEstimado;

  @Embedded private DireccionEmbeddable destino;
}
