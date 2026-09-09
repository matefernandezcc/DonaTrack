package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("HUMANA")
@Getter
@Setter
public class PersonaHumanaEntity extends PersonaEntity {

  @Column(name = "nombre")
  private String nombre;

  @Column(name = "apellido")
  private String apellido;

  @Column(name = "fecha_nacimiento")
  private LocalDate fechaNacimiento;
}
