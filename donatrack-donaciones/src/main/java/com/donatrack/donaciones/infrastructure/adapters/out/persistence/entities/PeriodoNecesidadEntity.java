package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "periodos_necesidad", schema = "donaciones")
@Getter
@Setter
public class PeriodoNecesidadEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "periodo_necesidad_id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "necesidad_id")
  private NecesidadEntity necesidad;

  @Column(name = "fecha_inicio")
  private LocalDate fechaInicio;

  @Column(name = "fecha_fin")
  private LocalDate fechaFin;

  @Column(name = "estado")
  private String estado;
}
