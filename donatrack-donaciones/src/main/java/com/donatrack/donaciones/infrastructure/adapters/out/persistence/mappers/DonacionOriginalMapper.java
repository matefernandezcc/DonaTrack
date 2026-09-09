package com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionOriginalEntity;

public class DonacionOriginalMapper {

  public static DonacionOriginalEntity toEntity(DonacionOriginal domain) {
    if (domain == null) return null;

    DonacionOriginalEntity entity = new DonacionOriginalEntity();
    entity.setId(domain.getId());
    entity.setFechaRecepcion(domain.getFechaRecepcion().atStartOfDay()); // Domain has LocalDate, Entity has LocalDateTime

    // The donante is mapped as RolEntity (DonanteEntity)
    if (domain.getDonante() != null) {
      entity.setDonante((com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonanteEntity) RolMapper.toEntity(domain.getDonante()));
    }

    return entity;
  }

  public static DonacionOriginal toDomain(DonacionOriginalEntity entity) {
    if (entity == null) return null;

    DonacionOriginal domain = new DonacionOriginal(
        null, // descripcionGeneral
        null, // donante
        null  // usuarioId
    );
    domain.setId(entity.getId());
    domain.setFechaRecepcion(entity.getFechaRecepcion().toLocalDate());

    if (entity.getDonante() != null) {
      domain.setDonante((Donante) RolMapper.toDomain(entity.getDonante()));
    }

    return domain;
  }
}
