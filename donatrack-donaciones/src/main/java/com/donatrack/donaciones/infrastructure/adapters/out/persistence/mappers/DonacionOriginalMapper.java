package com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionOriginalEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonanteEntity;

public class DonacionOriginalMapper {

  public static DonacionOriginalEntity toEntity(DonacionOriginal domain) {
    if (domain == null) return null;

    DonacionOriginalEntity entity = new DonacionOriginalEntity();
    entity.setId(domain.getId());
    entity.setDescripcionGeneral(domain.getDescripcionGeneral());
    entity.setUsuarioId(domain.getUsuarioId());
    if (domain.getFechaRecepcion() != null) {
      entity.setFechaRecepcion(domain.getFechaRecepcion().atStartOfDay());
    }

    if (domain.getDonante() != null) {
      entity.setDonante((DonanteEntity) RolMapper.toEntity(domain.getDonante()));
    }

    return entity;
  }

  public static DonacionOriginal toDomain(DonacionOriginalEntity entity) {
    if (entity == null) return null;

    DonacionOriginal domain = new DonacionOriginal(
        entity.getDescripcionGeneral(),
        null,
        entity.getUsuarioId()
    );
    domain.setId(entity.getId());
    if (entity.getFechaRecepcion() != null) {
      domain.setFechaRecepcion(entity.getFechaRecepcion().toLocalDate());
    }

    if (entity.getDonante() != null) {
      domain.setDonante((Donante) RolMapper.toDomain(entity.getDonante()));
    }

    return domain;
  }
}
