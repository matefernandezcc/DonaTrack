package com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.logistica.domain.entities.reparto.Camion;
import com.donatrack.logistica.domain.entities.reparto.Chofer;
import com.donatrack.logistica.domain.entities.reparto.Coordenada;
import com.donatrack.logistica.domain.entities.reparto.Direccion;
import com.donatrack.logistica.domain.entities.reparto.Parada;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.CoordenadaEmbeddable;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.DireccionEmbeddable;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.ParadaEntity;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.RutaDeRepartoEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper bidireccional entre RutaDeReparto (dominio) y RutaDeRepartoEntity (JPA). Incluye el mapeo
 * recursivo de Paradas, Entregas, Direcciones y Coordenadas.
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
        pe.setRuta(entity); // Establece la relación bidireccional
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
      entity.setDireccion(
          new DireccionEmbeddable(
              domain.getDireccion().getCalle(),
              domain.getDireccion().getAltura(),
              domain.getDireccion().getLocalidad()));
    }

    if (domain.getCoordenada() != null) {
      entity.setCoordenada(
          new CoordenadaEmbeddable(
              domain.getCoordenada().getLatitud(), domain.getCoordenada().getLongitud()));
    }

    if (domain.getEntregas() != null) {
      var entregasEntity =
          domain.getEntregas().stream()
              .map(
                  e -> {
                    var ee = EntregaMapper.toEntity(e);
                    ee.setParada(entity); // Relación bidireccional
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

    if (entity.getDireccion() != null) {
      domain.setDireccion(
          new Direccion(
              entity.getDireccion().getCalle(),
              entity.getDireccion().getAlturaDir(),
              entity.getDireccion().getLocalidad()));
    }

    if (entity.getCoordenada() != null) {
      domain.setCoordenada(
          new Coordenada(entity.getCoordenada().getLatitud(), entity.getCoordenada().getLongitud()));
    }

    if (entity.getEntregas() != null) {
      domain.setEntregas(
          new ArrayList<>(entity.getEntregas().stream().map(EntregaMapper::toDomain).toList()));
    }

    return domain;
  }
}
