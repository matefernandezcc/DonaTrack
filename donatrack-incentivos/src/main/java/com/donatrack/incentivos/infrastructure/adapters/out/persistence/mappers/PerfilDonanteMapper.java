package com.donatrack.incentivos.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.incentivos.domain.entities.Insignia;
import com.donatrack.incentivos.domain.entities.PerfilDonante;
import com.donatrack.incentivos.domain.entities.RegistroDonacion;
import com.donatrack.incentivos.domain.entities.categoria.CategoriaDonante;
import com.donatrack.incentivos.domain.entities.misiones.Mision;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.InsigniaEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.InsigniaObtenidaEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.MetricasDonanteEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.MisionEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.PerfilDonanteEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.ProgresoMisionEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.RegistroDonacionEntity;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PerfilDonanteMapper {

  public static PerfilDonanteEntity toEntity(PerfilDonante domain) {
    if (domain == null) return null;

    PerfilDonanteEntity entity = new PerfilDonanteEntity();
    entity.setPerfilDonanteId(domain.getDonanteId());
    if (domain.getCategoria() != null) {
      entity.setCategoria(domain.getCategoria().name());
    }

    // Misión actual
    if (domain.getMisionActual() != null) {
      MisionEntity me = new MisionEntity();
      me.setNombre(domain.getMisionActual().getNombre());
      if (domain.getMisionActual().getTipoMetrica() != null) {
        me.setTipoMetrica(domain.getMisionActual().getTipoMetrica().name());
      }
      me.setObjetivo(domain.getMisionActual().getObjetivo());
      entity.setMisionActual(me);
    }

    // Insignias obtenidas
    if (domain.getInsigniasObtenidas() != null) {
      List<InsigniaObtenidaEntity> list = new ArrayList<>();
      for (Insignia ins : domain.getInsigniasObtenidas()) {
        InsigniaEntity ie = new InsigniaEntity();
        ie.setId(ins.getId());
        ie.setNombre(ins.getNombre());
        ie.setDescripcion(ins.getDescripcion());

        InsigniaObtenidaEntity ioe = new InsigniaObtenidaEntity();
        ioe.setPerfil(entity);
        ioe.setInsignia(ie);
        ioe.setFechaObtencion(ins.getFechaObtencion());
        ioe.setVisiblePublicamente(ins.isVisiblePublicamente());
        list.add(ioe);
      }
      entity.setInsigniasObtenidas(list);
    }

    // Métricas del donante
    if (domain.getMetricas() != null) {
      MetricasDonanteEntity mde = new MetricasDonanteEntity();
      mde.setPerfil(entity);
      mde.setTotalDonacionesHistoricas(
          domain.getMetricas().getRegistrosDonacion() != null
              ? domain.getMetricas().getRegistrosDonacion().size()
              : 0);

      // Registros de donación
      if (domain.getMetricas().getRegistrosDonacion() != null) {
        List<RegistroDonacionEntity> regEntities = domain.getMetricas().getRegistrosDonacion().stream().map(reg -> {
          RegistroDonacionEntity rde = new RegistroDonacionEntity();
          rde.setMetricas(mde);
          rde.setIdDonacionOrigen(reg.getIdDonacion());
          rde.setCantidadBienes(reg.getCantidadBienes());
          rde.setCategorias(reg.getCategorias() != null ? String.join(",", reg.getCategorias()) : null);
          rde.setIdEntidadBeneficiariaOrigen(reg.getIdEntidadBeneficiaria());
          rde.setMesDonacion(reg.getMesDonacion() != null ? reg.getMesDonacion().toString() : null);
          return rde;
        }).collect(Collectors.toList());
        mde.setRegistrosDonacion(regEntities);
      }
      entity.setMetricas(mde);

      // Misiones completadas -> ProgresoMisionEntity
      if (domain.getMetricas().getMisionesCompletadas() != null) {
        List<ProgresoMisionEntity> progresos = new ArrayList<>();
        for (Map.Entry<Mision, YearMonth> entry : domain.getMetricas().getMisionesCompletadas().entrySet()) {
          ProgresoMisionEntity pme = new ProgresoMisionEntity();
          pme.setPerfil(entity);
          MisionEntity me = new MisionEntity();
          me.setNombre(entry.getKey().getNombre());
          pme.setMision(me);
          pme.setEstado("COMPLETADA");
          pme.setMesCompletada(entry.getValue().toString());
          progresos.add(pme);
        }
        entity.setProgresosMisiones(progresos);
      }
    }

    return entity;
  }

  public static PerfilDonante toDomain(PerfilDonanteEntity entity) {
    if (entity == null) return null;

    PerfilDonante domain = new PerfilDonante(entity.getPerfilDonanteId());

    if (entity.getCategoria() != null) {
      try {
        domain.setCategoria(CategoriaDonante.valueOf(entity.getCategoria().trim().toUpperCase()));
      } catch (Exception e) {
        domain.setCategoria(CategoriaDonante.COLABORADOR);
      }
      domain.cargarMisionesDeCategoriaActual();
    }

    // Insignias obtenidas
    if (entity.getInsigniasObtenidas() != null) {
      domain.setInsigniasObtenidas(entity.getInsigniasObtenidas().stream().map(ioe -> {
        Insignia ins = new Insignia(
            ioe.getInsignia() != null ? ioe.getInsignia().getNombre() : "Insignia",
            ioe.getInsignia() != null ? ioe.getInsignia().getDescripcion() : ""
        );
        if (ioe.getInsignia() != null) {
          ins.setId(ioe.getInsignia().getId());
        }
        ins.setFechaObtencion(ioe.getFechaObtencion());
        ins.setVisiblePublicamente(ioe.getVisiblePublicamente() != null ? ioe.getVisiblePublicamente() : true);
        return ins;
      }).collect(Collectors.toList()));
    }

    // Registros donación
    if (entity.getMetricas() != null && entity.getMetricas().getRegistrosDonacion() != null) {
      domain.getMetricas().setRegistrosDonacion(entity.getMetricas().getRegistrosDonacion().stream().map(rde -> {
        RegistroDonacion reg = new RegistroDonacion();
        reg.setIdDonacion(rde.getIdDonacionOrigen());
        reg.setCantidadBienes(rde.getCantidadBienes() != null ? rde.getCantidadBienes() : 0);
        if (rde.getCategorias() != null && !rde.getCategorias().isBlank()) {
          reg.setCategorias(new HashSet<>(Arrays.asList(rde.getCategorias().split(","))));
        } else {
          reg.setCategorias(new HashSet<>());
        }
        reg.setIdEntidadBeneficiaria(rde.getIdEntidadBeneficiariaOrigen());
        reg.setMesDonacion(rde.getMesDonacion() != null ? YearMonth.parse(rde.getMesDonacion()) : null);
        return reg;
      }).collect(Collectors.toList()));
    }

    // Progreso misiones
    if (entity.getProgresosMisiones() != null) {
      Map<Mision, YearMonth> map = domain.getMetricas().getMisionesCompletadas();
      for (ProgresoMisionEntity pme : entity.getProgresosMisiones()) {
        if ("COMPLETADA".equalsIgnoreCase(pme.getEstado()) && pme.getMesCompletada() != null && pme.getMision() != null) {
          Mision dummy = new Mision(pme.getMision().getNombre(), null, null, 0);
          map.put(dummy, YearMonth.parse(pme.getMesCompletada()));
        }
      }

      while (domain.getMisionActual() != null && map.containsKey(domain.getMisionActual())) {
        domain.setMisionActual(domain.getMisionesPendientes().poll());
      }
    }

    return domain;
  }
}
