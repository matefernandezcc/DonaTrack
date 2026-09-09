package com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import com.donatrack.donaciones.domain.entities.roles.Rol;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.BeneficiarioEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonanteEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.RolEntity;

public class RolMapper {

  public static RolEntity toEntity(Rol domain) {
    if (domain == null) return null;

    RolEntity entity;
    if (domain instanceof Donante) {
      entity = new DonanteEntity();
    } else if (domain instanceof Beneficiario b) {
      BeneficiarioEntity be = new BeneficiarioEntity();
      be.setCorreoRepresentante(b.getCorreoRepresentante());
      entity = be;
    } else {
      // Representante is another role, but for brevity, we handle basic ones
      throw new IllegalArgumentException("Rol no soportado: " + domain.getClass().getSimpleName());
    }

    entity.setId(domain.getId());
    entity.setFechaAlta(domain.getFechaAlta());
    return entity;
  }

  public static Rol toDomain(RolEntity entity) {
    if (entity == null) return null;

    Rol domain;
    if (entity instanceof DonanteEntity) {
      domain = new Donante();
    } else if (entity instanceof BeneficiarioEntity be) {
      Beneficiario b = new Beneficiario();
      b.setCorreoRepresentante(be.getCorreoRepresentante());
      domain = b;
    } else {
      throw new IllegalArgumentException("RolEntity no soportado");
    }

    domain.setId(entity.getId());
    domain.setFechaAlta(entity.getFechaAlta());
    return domain;
  }
}
