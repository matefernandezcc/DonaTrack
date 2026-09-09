package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("JURIDICA")
@Getter
@Setter
public class PersonaJuridicaEntity extends PersonaEntity {

  @Column(name = "razon_social")
  private String razonSocial;

  @Column(name = "tipo_juridica")
  private String tipo;

  @Column(name = "cuit")
  private String cuit;

  @Column(name = "fecha_constitucion")
  private LocalDate fechaConstitucion;
}
