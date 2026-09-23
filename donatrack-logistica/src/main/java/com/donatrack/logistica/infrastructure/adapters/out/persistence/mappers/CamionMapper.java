package com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.logistica.domain.entities.reparto.Camion;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.CamionEntity;

public final class CamionMapper {

  private CamionMapper() {}

  public static CamionEntity toEntity(Camion domain) {
    if (domain == null) return null;
    return new CamionEntity(
        null,
        domain.getPatente(),
        domain.getCapacidadVolumen(),
        domain.getAltura(),
        domain.getCapacidadCarga());
  }

  public static Camion toDomain(CamionEntity entity) {
    if (entity == null) return null;
    return new Camion(
        entity.getPatente(),
        entity.getCapacidadVolumen(),
        entity.getAltura(),
        entity.getCapacidadCarga());
  }
}
