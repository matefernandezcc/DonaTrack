package com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.logistica.domain.entities.planificacion.ItemPlanificacion;
import com.donatrack.logistica.domain.entities.reparto.Direccion;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.DireccionEmbeddable;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.ItemPlanificacionEntity;

/**
 * Mapper bidireccional entre las domain entities y las JPA entities. Mantiene la capa de dominio
 * desacoplada de JPA.
 */
public final class ItemPlanificacionMapper {

  private ItemPlanificacionMapper() {}

  public static ItemPlanificacionEntity toEntity(ItemPlanificacion domain) {
    ItemPlanificacionEntity entity = new ItemPlanificacionEntity();
    entity.setIdDonacionOriginal(domain.getIdDonacionOriginal());
    entity.setPesoEstimado(domain.getPesoEstimado());
    entity.setVolumenEstimado(domain.getVolumenEstimado());

    if (domain.getDestino() != null) {
      DireccionEmbeddable dir = new DireccionEmbeddable();
      dir.setCalle(domain.getDestino().getCalle());
      dir.setAlturaDir(domain.getDestino().getAltura());
      dir.setLocalidad(domain.getDestino().getLocalidad());
      entity.setDestino(dir);
    }

    return entity;
  }

  public static ItemPlanificacion toDomain(ItemPlanificacionEntity entity) {
    Direccion destino = null;
    if (entity.getDestino() != null) {
      destino =
          new Direccion(
              entity.getDestino().getCalle(),
              entity.getDestino().getAlturaDir(),
              entity.getDestino().getLocalidad());
    }

    return new ItemPlanificacion(
        entity.getIdDonacionOriginal(),
        entity.getPesoEstimado(),
        entity.getVolumenEstimado(),
        destino);
  }
}
