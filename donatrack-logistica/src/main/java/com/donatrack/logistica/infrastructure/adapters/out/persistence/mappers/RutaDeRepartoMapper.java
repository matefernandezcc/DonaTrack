package com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.logistica.domain.entities.reparto.Coordenada;
import com.donatrack.logistica.domain.entities.reparto.Direccion;
import com.donatrack.logistica.domain.entities.reparto.Parada;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.ParadaEntity;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.RutaDeRepartoEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper bidireccional entre RutaDeReparto (dominio) y RutaDeRepartoEntity (JPA).
 */
public final class RutaDeRepartoMapper {

  private RutaDeRepartoMapper() {}

  // ========== RUTA ==========

  public static RutaDeRepartoEntity toEntity(RutaDeReparto domain) {
    if (domain == null) return null;

    RutaDeRepartoEntity entity = new RutaDeRepartoEntity();
    entity.setId(domain.getId());
    entity.setFechaOperativa(domain.getFechaOperativa());
    entity.setIniciada(domain.getIniciada() != null ? domain.getIniciada() : false);

    if (domain.getCamion() != null) {
      entity.setCamion(CamionMapper.toEntity(domain.getCamion()));
    }
    if (domain.getChofer() != null) {
      entity.setChofer(ChoferMapper.toEntity(domain.getChofer()));
    }

    if (domain.getParadas() != null) {
      List<ParadaEntity> paradasEntity = new ArrayList<>();
      for (Parada parada : domain.getParadas()) {
        ParadaEntity pe = toParadaEntity(parada);
        pe.setRuta(entity);
        paradasEntity.add(pe);
      }
      entity.setParadas(paradasEntity);
    }

    return entity;
  }

  public static RutaDeReparto toDomain(RutaDeRepartoEntity entity) {
    if (entity == null) return null;

    RutaDeReparto domain = new RutaDeReparto();
    domain.setId(entity.getId());
    domain.setFechaOperativa(entity.getFechaOperativa());
    domain.setIniciada(entity.getIniciada());

    if (entity.getCamion() != null) {
      domain.setCamion(CamionMapper.toDomain(entity.getCamion()));
    }
    if (entity.getChofer() != null) {
      domain.setChofer(ChoferMapper.toDomain(entity.getChofer()));
    }

    if (entity.getParadas() != null) {
      List<Parada> paradasDomain = new ArrayList<>();
      for (ParadaEntity pe : entity.getParadas()) {
        paradasDomain.add(toParadaDomain(pe));
      }
      domain.setParadas(paradasDomain);
    }

    return domain;
  }

  // ========== PARADA ==========

  private static ParadaEntity toParadaEntity(Parada domain) {
    ParadaEntity entity = new ParadaEntity();
    entity.setOrden(domain.getOrden());

    if (domain.getDireccion() != null) {
      entity.setCalle(domain.getDireccion().getCalle());
      entity.setAltura(domain.getDireccion().getAltura());
      entity.setLocalidad(domain.getDireccion().getLocalidad());
    }

    if (domain.getCoordenada() != null) {
      entity.setLatitud(domain.getCoordenada().getLatitud());
      entity.setLongitud(domain.getCoordenada().getLongitud());
    }

    if (domain.getEntregas() != null) {
      var entregasEntity =
          domain.getEntregas().stream()
              .map(
                  e -> {
                    var ee = EntregaMapper.toEntity(e);
                    ee.setParada(entity);
                    return ee;
                  })
              .toList();
      entity.setEntregas(new ArrayList<>(entregasEntity));
    }

    return entity;
  }

  private static Parada toParadaDomain(ParadaEntity entity) {
    Parada domain = new Parada();
    domain.setOrden(entity.getOrden());

    if (entity.getCalle() != null || entity.getAltura() != null || entity.getLocalidad() != null) {
      domain.setDireccion(
          new Direccion(
              entity.getCalle(),
              entity.getAltura(),
              entity.getLocalidad()));
    }

    if (entity.getLatitud() != null && entity.getLongitud() != null) {
      domain.setCoordenada(
          new Coordenada(entity.getLatitud(), entity.getLongitud()));
    }

    if (entity.getEntregas() != null) {
      domain.setEntregas(
          new ArrayList<>(entity.getEntregas().stream().map(EntregaMapper::toDomain).toList()));
    }

    return domain;
  }
}
