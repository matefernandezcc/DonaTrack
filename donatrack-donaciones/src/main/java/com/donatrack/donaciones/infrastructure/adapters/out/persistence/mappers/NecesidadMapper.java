package com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.donaciones.domain.entities.necesidades.Necesidad;
import com.donatrack.donaciones.domain.entities.necesidades.NecesidadExtraordinaria;
import com.donatrack.donaciones.domain.entities.necesidades.NecesidadRecurrente;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.NecesidadEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.NecesidadExtraordinariaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.NecesidadRecurrenteEntity;

public class NecesidadMapper {

  public static NecesidadEntity toEntity(Necesidad domain) {
    if (domain == null) return null;
    
    NecesidadEntity entity;
    if (domain instanceof NecesidadExtraordinaria ex) {
      NecesidadExtraordinariaEntity exe = new NecesidadExtraordinariaEntity();
      exe.setCantidadRequerida(ex.getCantidadRequerida());
      if (ex.getEstado() != null) {
        exe.setEstado(ex.getEstado().name());
      }
      entity = exe;
    } else if (domain instanceof NecesidadRecurrente rec) {
      NecesidadRecurrenteEntity rece = new NecesidadRecurrenteEntity();
      rece.setCantidadObjetivo(rec.getCantidadObjetivo());
      rece.setActiva(rec.getActiva());
      if (rec.getTipoPeriodo() != null) {
        rece.setTipoPeriodo(rec.getTipoPeriodo().name());
      }
      entity = rece;
    } else {
      throw new IllegalArgumentException("Necesidad no soportada");
    }

    entity.setId(domain.getId());
    entity.setDescripcion(domain.getDescripcion());
    if (domain.getFechaSolicitud() != null) {
        entity.setFechaSolicitud(domain.getFechaSolicitud().atStartOfDay());
    }
    // NOTA: subcategoriaRequerida no se está mapeando aquí para evitar circularidad
    // dependiendo de cómo se resuelva
    return entity;
  }

  public static Necesidad toDomain(NecesidadEntity entity) {
    if (entity == null) return null;
    
    Necesidad domain;
    if (entity instanceof NecesidadExtraordinariaEntity exe) {
      NecesidadExtraordinaria ex = new NecesidadExtraordinaria();
      ex.setCantidadRequerida(exe.getCantidadRequerida() != null ? exe.getCantidadRequerida() : 0.0);
      domain = ex;
    } else if (entity instanceof NecesidadRecurrenteEntity rece) {
      NecesidadRecurrente rec = new NecesidadRecurrente();
      rec.setCantidadObjetivo(rece.getCantidadObjetivo() != null ? rece.getCantidadObjetivo() : 0.0);
      rec.setActiva(rece.getActiva() != null ? rece.getActiva() : false);
      domain = rec;
    } else {
      throw new IllegalArgumentException("NecesidadEntity no soportada");
    }

    domain.setId(entity.getId());
    domain.setDescripcion(entity.getDescripcion());
    if (entity.getFechaSolicitud() != null) {
        domain.setFechaSolicitud(entity.getFechaSolicitud().toLocalDate());
    }
    return domain;
  }
}
