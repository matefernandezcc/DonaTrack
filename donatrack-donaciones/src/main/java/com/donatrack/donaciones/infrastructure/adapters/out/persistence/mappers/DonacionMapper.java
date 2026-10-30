package com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.HistorialEstado;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.HistorialEstadoEntity;
import java.time.LocalDateTime;

public class DonacionMapper {

  public static DonacionEntity toEntity(Donacion domain) {
    if (domain == null) return null;

    DonacionEntity entity = new DonacionEntity();
    entity.setId(domain.getId());
    if (domain.getEstado() != null) {
      entity.setEstado(domain.getEstado().name());
    }
    if (domain.getFechaCreacion() != null) {
      entity.setFechaCreacion(domain.getFechaCreacion().atStartOfDay());
    } else {
      entity.setFechaCreacion(LocalDateTime.now());
    }

    if (domain.getBienes() != null) {
      domain
          .getBienes()
          .forEach(
              bien -> {
                var bienEntity = BienMapper.toEntity(bien);
                if (bienEntity != null) {
                  bienEntity.setDonacion(entity);
                  entity.getBienes().add(bienEntity);
                }
              });
    }

    if (domain.getHistorial() != null) {
      domain
          .getHistorial()
          .forEach(
              h -> {
                HistorialEstadoEntity he = new HistorialEstadoEntity();
                he.setDonacion(entity);
                if (h.getEstado() != null) {
                  he.setEstado(h.getEstado().name());
                }
                he.setFecha(
                    h.getFecha() != null ? h.getFecha().atStartOfDay() : LocalDateTime.now());
                he.setObservacion(h.getObservacion());
                he.setUsuarioId(h.getUsuarioId());
                entity.getHistorialEstados().add(he);
              });
    }

    return entity;
  }

  public static Donacion toDomain(DonacionEntity entity) {
    if (entity == null) return null;

    Donacion domain = new Donacion(null);
    domain.setId(entity.getId());
    if (entity.getEstado() != null) {
      domain.setEstado(EstadoDonacion.valueOf(entity.getEstado()));
    }
    if (entity.getFechaCreacion() != null) {
      domain.setFechaCreacion(entity.getFechaCreacion().toLocalDate());
    }

    if (entity.getNecesidad() != null && entity.getNecesidad().getBeneficiario() != null) {
      domain.setEntidadAsignada(
          (Beneficiario) RolMapper.toDomain(entity.getNecesidad().getBeneficiario()));
    }

    if (entity.getSubcategoriaAsignada() != null) {
      domain.setSubCategoria(
          new Subcategoria(
              entity.getSubcategoriaAsignada().getNombre(),
              entity.getSubcategoriaAsignada().getDescripcion()));
    }

    if (entity.getBienes() != null) {
      entity
          .getBienes()
          .forEach(
              bienEntity -> {
                var bien = BienMapper.toDomain(bienEntity);
                if (bien != null) {
                  if (bien.getSubcategoria() == null && domain.getSubCategoria() != null) {
                    bien.setSubcategoria(domain.getSubCategoria());
                  }
                  domain.agregarBien(bien);
                }
              });
    }

    if (entity.getHistorialEstados() != null) {
      entity
          .getHistorialEstados()
          .forEach(
              he -> {
                EstadoDonacion est =
                    he.getEstado() != null ? EstadoDonacion.valueOf(he.getEstado()) : null;
                HistorialEstado h =
                    new HistorialEstado(est, he.getObservacion(), he.getUsuarioId());
                if (he.getFecha() != null) {
                  h.setFecha(he.getFecha().toLocalDate());
                }
                domain.registrarCambioEstado(h);
              });
    }

    return domain;
  }
}
