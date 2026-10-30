package com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo;

import com.donatrack.logistica.domain.entities.planificacion.SolicitudPlanificacion;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.SolicitudPlanificacionDocument;
import java.util.ArrayList;
import java.util.UUID;

public final class SolicitudPlanificacionMongoMapper {

  private SolicitudPlanificacionMongoMapper() {}

  public static SolicitudPlanificacionDocument toDocument(SolicitudPlanificacion domain) {
    if (domain == null) return null;

    UUID id = domain.getId() != null ? domain.getId() : UUID.randomUUID();
    return new SolicitudPlanificacionDocument(
        id,
        domain.getFechaSolicitud(),
        domain.getEstado(),
        domain.getIdsDonaciones() != null ? new ArrayList<>(domain.getIdsDonaciones()) : new ArrayList<>());
  }

  public static SolicitudPlanificacion toDomain(SolicitudPlanificacionDocument doc) {
    if (doc == null) return null;

    SolicitudPlanificacion domain = new SolicitudPlanificacion();
    domain.setId(doc.getId());
    domain.setFechaSolicitud(doc.getFechaSolicitud());
    domain.setEstado(doc.getEstado());
    domain.setIdsDonaciones(doc.getIdsDonaciones() != null ? new ArrayList<>(doc.getIdsDonaciones()) : new ArrayList<>());
    return domain;
  }
}
