package com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import com.donatrack.donaciones.domain.entities.roles.Representante;
import com.donatrack.donaciones.domain.entities.roles.Rol;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.BeneficiarioEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonanteEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.RepresentanteEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.RolEntity;

public class RolMapper {

  public static RolEntity toEntity(Rol domain) {
    if (domain == null) return null;

    RolEntity entity;
    if (domain instanceof Donante) {
      entity = new DonanteEntity();
    } else if (domain instanceof Beneficiario) {
      entity = new BeneficiarioEntity();
    } else if (domain instanceof Representante r) {
      RepresentanteEntity re = new RepresentanteEntity();
      re.setCargo(r.getCargo());
      entity = re;
    } else {
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
    } else if (entity instanceof BeneficiarioEntity) {
      domain = new Beneficiario();
    } else if (entity instanceof RepresentanteEntity re) {
      domain = new Representante(re.getCargo(), null);
    } else {
      throw new IllegalArgumentException(
          "RolEntity no soportado: " + entity.getClass().getSimpleName());
    }

    domain.setId(entity.getId());
    domain.setFechaAlta(entity.getFechaAlta());
    return domain;
  }
}
