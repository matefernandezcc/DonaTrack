package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles", schema = "donaciones")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_rol", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class RolEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "fecha_alta", nullable = false)
  private LocalDate fechaAlta;

  @ManyToOne
  @JoinColumn(name = "persona_id")
  private PersonaEntity persona;
}
