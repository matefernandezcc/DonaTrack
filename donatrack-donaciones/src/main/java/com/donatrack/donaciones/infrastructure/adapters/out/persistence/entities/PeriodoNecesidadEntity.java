package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "periodos_necesidad", schema = "donaciones")
@Getter
@Setter
public class PeriodoNecesidadEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "fecha_inicio", nullable = false)
  private LocalDate fechaInicio;

  @Column(name = "fecha_fin", nullable = false)
  private LocalDate fechaFin;

  @Column(name = "estado", nullable = false)
  private String estado;

  @ManyToOne
  @JoinColumn(name = "necesidad_recurrente_id")
  private NecesidadRecurrenteEntity necesidadRecurrente;

  @ManyToMany
  @JoinTable(
      name = "periodos_necesidad_donaciones",
      schema = "donaciones",
      joinColumns = @JoinColumn(name = "periodo_id"),
      inverseJoinColumns = @JoinColumn(name = "donacion_id")
  )
  private List<DonacionEntity> donacionesAsignadas;
}
