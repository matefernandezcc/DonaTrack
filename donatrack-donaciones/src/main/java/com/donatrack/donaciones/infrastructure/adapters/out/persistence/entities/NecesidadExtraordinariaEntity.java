package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "necesidades_extraordinarias", schema = "donaciones")
@Getter
@Setter
public class NecesidadExtraordinariaEntity extends NecesidadEntity {

  @Column(name = "cantidad_requerida", nullable = false)
  private double cantidadRequerida;

  @Column(name = "estado", nullable = false)
  private String estado;

  @ManyToMany
  @JoinTable(
      name = "necesidades_extraordinarias_donaciones",
      schema = "donaciones",
      joinColumns = @JoinColumn(name = "necesidad_id"),
      inverseJoinColumns = @JoinColumn(name = "donacion_id")
  )
  private List<DonacionEntity> donacionesRecibidas;
}
