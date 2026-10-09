package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles_beneficiario", schema = "donaciones")
@PrimaryKeyJoinColumn(name = "rol_beneficiario_id", referencedColumnName = "rol_id")
@DiscriminatorValue("Beneficiario")
@Getter
@Setter
public class BeneficiarioEntity extends RolEntity {
  @jakarta.persistence.Column(name = "correo_representante")
  private String correoRepresentante;

  @jakarta.persistence.OneToMany(mappedBy = "beneficiario", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
  private java.util.List<NecesidadEntity> necesidadesDeclaradas = new java.util.ArrayList<>();
}
