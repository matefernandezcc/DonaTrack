package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "necesidades_recurrentes", schema = "donaciones")
@Getter
@Setter
public class NecesidadRecurrenteEntity extends NecesidadEntity {

  @Column(name = "cantidad_objetivo", nullable = false)
  private double cantidadObjetivo;

  @Column(name = "activa", nullable = false)
  private Boolean activa;

  @Column(name = "tipo_periodo", nullable = false)
  private String tipoPeriodo;

  @OneToMany(mappedBy = "necesidadRecurrente", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PeriodoNecesidadEntity> historialPeriodos;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "periodo_actual_id")
  private PeriodoNecesidadEntity periodoActual;
}
