package com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.logistica.domain.entities.reparto.Chofer;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.ChoferEntity;

/**
 * Mapper bidireccional entre Chofer (dominio) y ChoferEntity (JPA). Mantiene la capa de dominio
 * desacoplada de JPA.
 */
public final class ChoferMapper {

  private ChoferMapper() {}

  public static ChoferEntity toEntity(Chofer domain) {
    if (domain == null) return null;
    return new ChoferEntity(domain.getLegajo(), domain.getNombre());
  }

  public static Chofer toDomain(ChoferEntity entity) {
    if (entity == null) return null;
    return new Chofer(entity.getLegajo(), entity.getNombre());
  }
}
