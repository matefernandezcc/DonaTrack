package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

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
@Table(name = "direcciones", schema = "donaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DireccionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "direccion_id")
  private UUID id;

  @Column(name = "calle")
  private String calle;

  @Column(name = "altura")
  private Double altura;

  @Column(name = "localidad")
  private String localidad;

  @Column(name = "provincia")
  private String provincia;

  @Column(name = "pais")
  private String pais;

  @Column(name = "cp")
  private String cp;

  @Column(name = "latitud")
  private Double latitud;

  @Column(name = "longitud")
  private Double longitud;
}
