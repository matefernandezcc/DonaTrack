package com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionEntity;

public class DonacionMapper {

  public static DonacionEntity toEntity(Donacion domain) {
    if (domain == null) return null;

    DonacionEntity entity = new DonacionEntity();
    // No setear ID: @GeneratedValue lo genera al persistir
    if (domain.getEstado() != null) {
      entity.setEstado(domain.getEstado().name());
    }

    if (domain.getBienes() != null) {
      domain
          .getBienes()
          .forEach(
              bien -> {
                var bienEntity = BienMapper.toEntity(bien);
                if (bienEntity != null) {
                  bienEntity.setDonacion(entity);
                  entity.getBienes().add(bienEntity);
                }
              });
    }

    return entity;
  }

  public static Donacion toDomain(DonacionEntity entity) {
    if (entity == null) return null;

    Donacion domain = new Donacion(null);
    domain.setId(entity.getId());
    if (entity.getEstado() != null) {
      domain.setEstado(EstadoDonacion.valueOf(entity.getEstado()));
    }

    if (entity.getNecesidad() != null && entity.getNecesidad().getBeneficiario() != null) {
      domain.setEntidadAsignada(
          (Beneficiario) RolMapper.toDomain(entity.getNecesidad().getBeneficiario()));
    }

    if (entity.getBienes() != null) {
      entity
          .getBienes()
          .forEach(
              bienEntity -> {
                var bien = BienMapper.toDomain(bienEntity);
                if (bien != null) {
                  domain.agregarBien(bien);
                }
              });
    }

    return domain;
  }
}
