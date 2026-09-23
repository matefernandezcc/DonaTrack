package com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionOriginalEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonanteEntity;

public class DonacionOriginalMapper {

  public static DonacionOriginalEntity toEntity(DonacionOriginal domain) {
    if (domain == null) return null;

    DonacionOriginalEntity entity = new DonacionOriginalEntity();
    // No setear ID: @GeneratedValue lo genera al persistir
    entity.setDescripcionGeneral(domain.getDescripcionGeneral());
    entity.setUsuarioId(domain.getUsuarioId());
    if (domain.getFechaRecepcion() != null) {
      entity.setFechaRecepcion(domain.getFechaRecepcion().atStartOfDay());
    }

    // El donante se asigna en el Repository con getReference() para evitar entidad detached

    if (domain.getDonacionesSegmentadas() != null) {
      domain.getDonacionesSegmentadas().forEach(donacion -> {
        var donacionEntity = DonacionMapper.toEntity(donacion);
        if (donacionEntity != null) {
          donacionEntity.setDonacionOriginal(entity);
          entity.getDonaciones().add(donacionEntity);
        }
      });
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

    if (entity.getDonaciones() != null) {
      entity.getDonaciones().forEach(donacionEntity -> {
        var donacion = DonacionMapper.toDomain(donacionEntity);
        if (donacion != null) {
          domain.getDonacionesSegmentadas().add(donacion);
        }
      });
    }

    return domain;
  }
}
