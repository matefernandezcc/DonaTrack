package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "historial_estado", schema = "donaciones")
@Getter
@Setter
public class HistorialEstadoEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "historial_estado_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "donacion_id")
  private DonacionEntity donacion;

  @Column(name = "estado")
  private String estado;

  @Column(name = "fecha")
  private LocalDateTime fecha;

  @Column(name = "observacion", columnDefinition = "TEXT")
  private String observacion;

  @Column(name = "usuario_id")
  private String usuarioId;
}
