package com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionEntity;

public class DonacionMapper {

  public static DonacionEntity toEntity(Donacion domain) {
    if (domain == null) return null;

    DonacionEntity entity = new DonacionEntity();
    entity.setId(domain.getId());
    entity.setEstado(domain.getEstado().name());
    
    // El resto de los mapeos (bienes, subcategoria, etc.) se irían completando aquí
    // pero para cumplir con el repositorio básico mapearemos lo esencial
    
    if (domain.getEntidadAsignada() != null) {
      entity.setBeneficiario((com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.BeneficiarioEntity) RolMapper.toEntity(domain.getEntidadAsignada()));
    }

    return entity;
  }

  public static Donacion toDomain(DonacionEntity entity) {
    if (entity == null) return null;

    Donacion domain = new Donacion(null);
    domain.setId(entity.getId());
    domain.setEstado(EstadoDonacion.valueOf(entity.getEstado()));

    if (entity.getBeneficiario() != null) {
      domain.setEntidadAsignada((Beneficiario) RolMapper.toDomain(entity.getBeneficiario()));
    }

    return domain;
  }
}
