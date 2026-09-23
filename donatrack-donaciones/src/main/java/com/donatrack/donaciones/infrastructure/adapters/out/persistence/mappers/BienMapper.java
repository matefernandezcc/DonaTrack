package com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.donaciones.domain.entities.donacion.Bien;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.BienEntity;

public class BienMapper {

  public static BienEntity toEntity(Bien domain) {
    if (domain == null) return null;

    BienEntity entity = new BienEntity();
    // No setear ID: @GeneratedValue lo genera al persistir
    entity.setDescripcion(domain.getDescripcion());
    entity.setCantidad(domain.getCantidad());
    entity.setUnidadMedicion(domain.getUnidadMedicion());
    entity.setEsUsado(domain.getEsUsado());
    entity.setFechaVencimiento(domain.getFechaVencimiento());

    return entity;
  }

  public static Bien toDomain(BienEntity entity) {
    if (entity == null) return null;

    Bien domain = new Bien(
        entity.getDescripcion(),
        entity.getCantidad() != null ? entity.getCantidad() : 0.0,
        entity.getUnidadMedicion(),
        entity.getEsUsado() != null ? entity.getEsUsado() : false,
        entity.getFechaVencimiento()
    );
    domain.setSubcategoria(null); // Esto requeriría otro mapeo si es necesario

    return domain;
  }
}
