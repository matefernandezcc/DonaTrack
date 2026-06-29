package com.donatrack.logistica.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "choferes", schema = "logistica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChoferEntity {

  @Id
  @Column(name = "legajo", length = 20)
  private String legajo;

  @Column(name = "nombre", length = 100, nullable = false)
  private String nombre;
}
