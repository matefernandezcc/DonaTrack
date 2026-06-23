package com.donatrack.logistica.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rutas_reparto", schema = "logistica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RutaDeRepartoEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "solicitud_id")
  private SolicitudPlanificacionEntity solicitud;

  @Column(name = "fecha_operativa", nullable = false)
  private LocalDate fechaOperativa;

  @Column(name = "iniciada", nullable = false)
  private Boolean iniciada = false;

  @ManyToOne
  @JoinColumn(name = "camion_patente")
  private CamionEntity camion;

  @ManyToOne
  @JoinColumn(name = "chofer_legajo")
  private ChoferEntity chofer;

  @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("orden ASC")
  private List<ParadaEntity> paradas;
}
