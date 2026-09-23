package com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.logistica.domain.entities.planificacion.ItemPlanificacion;
import com.donatrack.logistica.domain.entities.reparto.Direccion;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.ItemPlanificacionEntity;

public final class ItemPlanificacionMapper {

  private ItemPlanificacionMapper() {}

  public static ItemPlanificacionEntity toEntity(ItemPlanificacion domain) {
    if (domain == null) return null;
    ItemPlanificacionEntity entity = new ItemPlanificacionEntity();
    entity.setIdDonacion(domain.getIdDonacionOriginal());
    entity.setPesoEstimado(domain.getPesoEstimado());
    entity.setVolumenEstimado(domain.getVolumenEstimado());

    if (domain.getDestino() != null) {
      entity.setCalleDestino(domain.getDestino().getCalle());
      entity.setAlturaDestino(domain.getDestino().getAltura());
      entity.setLocalidadDestino(domain.getDestino().getLocalidad());
    }

    return entity;
  }

  public static ItemPlanificacion toDomain(ItemPlanificacionEntity entity) {
    if (entity == null) return null;
    Direccion destino = null;
    if (entity.getCalleDestino() != null || entity.getAlturaDestino() != null || entity.getLocalidadDestino() != null) {
      destino = new Direccion(
          entity.getCalleDestino(),
          entity.getAlturaDestino(),
          entity.getLocalidadDestino());
    }

    return new ItemPlanificacion(
        entity.getIdDonacion(),
        entity.getPesoEstimado(),
        entity.getVolumenEstimado(),
        destino);
  }
}
