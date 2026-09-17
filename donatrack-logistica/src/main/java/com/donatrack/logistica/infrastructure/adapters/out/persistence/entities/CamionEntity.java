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
@Table(name = "camiones", schema = "logistica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CamionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "camion_id")
  private UUID id;

  @Column(name = "patente", length = 255, unique = true)
  private String patente;

  @Column(name = "capacidad_volumen")
  private Double capacidadVolumen;

  @Column(name = "altura")
  private Double altura;

  @Column(name = "capacidad_carga")
  private Double capacidadCarga;
}
