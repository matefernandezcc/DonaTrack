package com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.logistica.domain.entities.planificacion.SolicitudPlanificacion;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.SolicitudPlanificacionEntity;
import java.util.ArrayList;
import java.util.stream.Collectors;

public final class SolicitudPlanificacionMapper {

  private SolicitudPlanificacionMapper() {}

  public static SolicitudPlanificacionEntity toEntity(SolicitudPlanificacion domain) {
    if (domain == null) return null;

    SolicitudPlanificacionEntity entity = new SolicitudPlanificacionEntity();
    entity.setId(domain.getId());
    entity.setFechaSolicitud(domain.getFechaSolicitud());
    if (domain.getEstado() != null) {
      entity.setEstado(SolicitudPlanificacionEntity.EstadoPlanificacionEnum.valueOf(domain.getEstado().name()));
    }
    return entity;
  }

  public static SolicitudPlanificacion toDomain(SolicitudPlanificacionEntity entity) {
    if (entity == null) return null;

    SolicitudPlanificacion domain = new SolicitudPlanificacion();
    domain.setId(entity.getId());
    domain.setFechaSolicitud(entity.getFechaSolicitud());
    if (entity.getEstado() != null) {
      domain.setEstado(com.donatrack.logistica.domain.entities.planificacion.EstadoPlanificacion.valueOf(entity.getEstado().name()));
    }
    if (entity.getItems() != null) {
      domain.setIdsDonaciones(entity.getItems().stream().map(com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.ItemPlanificacionEntity::getIdDonacion).collect(Collectors.toList()));
    } else {
      domain.setIdsDonaciones(new ArrayList<>());
    }
    return domain;
  }
}
