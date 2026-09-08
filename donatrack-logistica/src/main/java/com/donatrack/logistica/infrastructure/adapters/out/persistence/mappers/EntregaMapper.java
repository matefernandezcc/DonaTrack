package com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.logistica.domain.entities.entregas.ComprobanteRecepcion;
import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.domain.entities.entregas.EstadoEntrega;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.ComprobanteRecepcionEmbeddable;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.EntregaEntity;

/**
 * Mapper bidireccional entre Entrega (dominio) y EntregaEntity (JPA). Incluye el mapeo del
 * ComprobanteRecepcion embeddable.
 */
public final class EntregaMapper {

  private EntregaMapper() {}

  public static EntregaEntity toEntity(Entrega domain) {
    if (domain == null) return null;

    EntregaEntity entity = new EntregaEntity();
    entity.setIdEntrega(domain.getIdEntrega());
    entity.setPesoEstimado(domain.getPesoEstimado());
    entity.setVolumenEstimado(domain.getVolumenEstimado());

    if (domain.getEstado() != null) {
      entity.setEstado(EntregaEntity.EstadoEntregaEnum.valueOf(domain.getEstado().name()));
    }

    if (domain.getComprobanteRecepcion() != null) {
      ComprobanteRecepcion cr = domain.getComprobanteRecepcion();
      entity.setComprobanteRecepcion(
          new ComprobanteRecepcionEmbeddable(
              cr.getFechaHora(), cr.getFotos(), cr.getCamionPatente()));
    }

    return entity;
  }

  public static Entrega toDomain(EntregaEntity entity) {
    if (entity == null) return null;

    Entrega domain = new Entrega();
    domain.setIdEntrega(entity.getIdEntrega());
    domain.setPesoEstimado(entity.getPesoEstimado());
    domain.setVolumenEstimado(entity.getVolumenEstimado());

    if (entity.getEstado() != null) {
      domain.setEstado(EstadoEntrega.valueOf(entity.getEstado().name()));
    }

    if (entity.getComprobanteRecepcion() != null) {
      ComprobanteRecepcionEmbeddable cre = entity.getComprobanteRecepcion();
      domain.setComprobanteRecepcion(
          new ComprobanteRecepcion(
              cre.getComprobanteFechaHora(),
              cre.getComprobanteFotos(),
              cre.getComprobanteCamionPatente()));
    }

    return domain;
  }
}
