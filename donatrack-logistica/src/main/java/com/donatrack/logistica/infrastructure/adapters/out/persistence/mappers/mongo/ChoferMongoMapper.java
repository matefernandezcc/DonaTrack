package com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo;

import com.donatrack.logistica.domain.entities.reparto.Chofer;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ChoferDocument;
import java.util.UUID;

public final class ChoferMongoMapper {

  private ChoferMongoMapper() {}

  public static ChoferDocument toDocument(Chofer domain) {
    if (domain == null) return null;
    UUID id = domain.getId() != null ? domain.getId() : UUID.randomUUID();
    return new ChoferDocument(id, domain.getLegajo(), domain.getNombre());
  }

  public static Chofer toDomain(ChoferDocument doc) {
    if (doc == null) return null;
    return new Chofer(doc.getId(), doc.getLegajo(), doc.getNombre());
  }
}
