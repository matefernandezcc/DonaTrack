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
@Table(name = "camiones", schema = "logistica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CamionEntity {

  @Id
  @Column(name = "patente", length = 10)
  private String patente;

  @Column(name = "capacidad_volumen")
  private Double capacidadVolumen;

  @Column(name = "altura")
  private Double altura;

  @Column(name = "capacidad_carga")
  private Double capacidadCarga;
}
