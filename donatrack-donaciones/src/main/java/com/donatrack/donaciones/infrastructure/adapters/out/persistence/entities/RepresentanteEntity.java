package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles_representante", schema = "donaciones")
@PrimaryKeyJoinColumn(name = "rol_representante_id", referencedColumnName = "rol_id")
@DiscriminatorValue("Representante")
@Getter
@Setter
public class RepresentanteEntity extends RolEntity {

  @Column(name = "cargo")
  private String cargo;

  @ManyToOne
  @JoinColumn(name = "organizacion_id")
  private PersonaJuridicaEntity organizacion;
}
