package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("BENEFICIARIO")
@Getter
@Setter
public class BeneficiarioEntity extends RolEntity {

  @Column(name = "correo_representante")
  private String correoRepresentante;

  // Las necesidades y donaciones asignadas están mapeadas del otro lado (NecesidadEntity y DonacionEntity)
}
