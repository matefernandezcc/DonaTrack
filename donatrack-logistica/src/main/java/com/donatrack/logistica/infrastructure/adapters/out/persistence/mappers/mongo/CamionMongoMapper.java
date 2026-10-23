package com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo;

import com.donatrack.logistica.domain.entities.reparto.Camion;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.CamionDocument;
import java.util.UUID;

public final class CamionMongoMapper {

  private CamionMongoMapper() {}

  public static CamionDocument toDocument(Camion domain) {
    if (domain == null) return null;
    UUID id = domain.getId() != null ? domain.getId() : UUID.randomUUID();
    return new CamionDocument(
        id,
        domain.getPatente(),
        domain.getCapacidadVolumen(),
        domain.getAltura(),
        domain.getCapacidadCarga());
  }

  public static Camion toDomain(CamionDocument doc) {
    if (doc == null) return null;
    return new Camion(
        doc.getId(),
        doc.getPatente(),
        doc.getCapacidadVolumen(),
        doc.getAltura(),
        doc.getCapacidadCarga());
  }
}
