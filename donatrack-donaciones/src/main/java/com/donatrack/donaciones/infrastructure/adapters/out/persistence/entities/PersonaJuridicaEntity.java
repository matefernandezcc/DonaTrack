package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "personas_juridicas", schema = "donaciones")
@PrimaryKeyJoinColumn(name = "persona_juridica_id", referencedColumnName = "persona_id")
@Getter
@Setter
public class PersonaJuridicaEntity extends PersonaEntity {

  @Column(name = "razon_social")
  private String razonSocial;

  @Column(name = "tipo")
  private String tipo;

  @Column(name = "rubro")
  private String rubro;
}
