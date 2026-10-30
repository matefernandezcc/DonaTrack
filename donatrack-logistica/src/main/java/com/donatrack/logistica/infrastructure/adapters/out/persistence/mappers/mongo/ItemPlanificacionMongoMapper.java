package com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo;

import com.donatrack.logistica.domain.entities.planificacion.ItemPlanificacion;
import com.donatrack.logistica.domain.entities.reparto.Direccion;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.DireccionDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ItemPlanificacionDocument;
import java.util.UUID;

public final class ItemPlanificacionMongoMapper {

  private ItemPlanificacionMongoMapper() {}

  public static ItemPlanificacionDocument toDocument(ItemPlanificacion domain) {
    if (domain == null) return null;

    DireccionDocument destinoDoc = null;
    if (domain.getDestino() != null) {
      destinoDoc =
          new DireccionDocument(
              domain.getDestino().getCalle(),
              domain.getDestino().getAltura(),
              domain.getDestino().getLocalidad());
    }

    return new ItemPlanificacionDocument(
        UUID.randomUUID(),
        domain.getIdDonacionOriginal(),
        domain.getPesoEstimado(),
        domain.getVolumenEstimado(),
        destinoDoc);
  }

  public static ItemPlanificacion toDomain(ItemPlanificacionDocument doc) {
    if (doc == null) return null;

    Direccion destino = null;
    if (doc.getDestino() != null) {
      destino =
          new Direccion(
              doc.getDestino().getCalle(),
              doc.getDestino().getAltura(),
              doc.getDestino().getLocalidad());
    }

    return new ItemPlanificacion(
        doc.getIdDonacionOriginal(), doc.getPesoEstimado(), doc.getVolumenEstimado(), destino);
  }
}
