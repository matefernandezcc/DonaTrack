package com.donatrack.donaciones.infrastructure.adapters.in.api.dtos;

import com.donatrack.donaciones.domain.entities.necesidades.Necesidad;
import com.donatrack.donaciones.domain.entities.necesidades.NecesidadExtraordinaria;
import com.donatrack.donaciones.domain.entities.necesidades.NecesidadRecurrente;

public class NecesidadDtoMapper {

  public static Necesidad toDomain(NecesidadDTO dto) {
    if (dto == null) return null;
    
    Necesidad domain;
    if (dto instanceof NecesidadExtraordinariaDTO exdto) {
      NecesidadExtraordinaria ex = new NecesidadExtraordinaria();
      ex.setCantidadRequerida(exdto.getCantidadRequerida());
      domain = ex;
    } else if (dto instanceof NecesidadRecurrenteDTO recdto) {
      NecesidadRecurrente rec = new NecesidadRecurrente();
      rec.setCantidadObjetivo(recdto.getCantidadObjetivo());
      rec.setTipoPeriodo(recdto.getTipoPeriodo());
      rec.setActiva(recdto.getActiva() != null ? recdto.getActiva() : true);
      domain = rec;
    } else {
      throw new IllegalArgumentException("DTO no soportado");
    }
    
    if (dto.getId() != null) domain.setId(dto.getId());
    domain.setDescripcion(dto.getDescripcion());
    if (dto.getFechaSolicitud() != null) domain.setFechaSolicitud(dto.getFechaSolicitud());
    domain.setSubcategoriaRequerida(dto.getSubcategoriaRequerida());
    // domain.setEstado(dto.getEstado()); // Si se necesita
    
    return domain;
  }

  public static NecesidadDTO toDto(Necesidad domain) {
    if (domain == null) return null;
    
    NecesidadDTO dto;
    if (domain instanceof NecesidadExtraordinaria ex) {
      NecesidadExtraordinariaDTO exdto = new NecesidadExtraordinariaDTO();
      exdto.setCantidadRequerida(ex.getCantidadRequerida());
      dto = exdto;
    } else if (domain instanceof NecesidadRecurrente rec) {
      NecesidadRecurrenteDTO recdto = new NecesidadRecurrenteDTO();
      recdto.setCantidadObjetivo(rec.getCantidadObjetivo());
      recdto.setTipoPeriodo(rec.getTipoPeriodo());
      recdto.setActiva(rec.getActiva());
      dto = recdto;
    } else {
      throw new IllegalArgumentException("Dominio no soportado");
    }
    
    dto.setId(domain.getId());
    dto.setDescripcion(domain.getDescripcion());
    dto.setFechaSolicitud(domain.getFechaSolicitud());
    dto.setSubcategoriaRequerida(domain.getSubcategoriaRequerida());
    
    return dto;
  }
}
