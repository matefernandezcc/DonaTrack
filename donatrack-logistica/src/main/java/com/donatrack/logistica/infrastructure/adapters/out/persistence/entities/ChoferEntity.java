package com.donatrack.logistica.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
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
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "chofer_id")
  private UUID id;

  @Column(name = "legajo", length = 255, unique = true)
  private String legajo;

  @Column(name = "nombre", length = 255, nullable = false)
  private String nombre;
}
