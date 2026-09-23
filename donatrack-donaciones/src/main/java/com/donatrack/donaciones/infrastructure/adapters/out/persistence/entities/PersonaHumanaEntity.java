package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "personas_humanas", schema = "donaciones")
@PrimaryKeyJoinColumn(name = "persona_humana_id", referencedColumnName = "persona_id")
@Getter
@Setter
public class PersonaHumanaEntity extends PersonaEntity {

  @Column(name = "nombre")
  private String nombre;

  @Column(name = "apellido")
  private String apellido;

  @Column(name = "edad")
  private Integer edad;

  @Column(name = "genero")
  private String genero;
}
