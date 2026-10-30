package com.donatrack.incentivos.infrastructure.adapters.out.persistence;

import com.donatrack.incentivos.application.ports.out.PerfilDonanteRepository;
import com.donatrack.incentivos.domain.entities.Insignia;
import com.donatrack.incentivos.domain.entities.PerfilDonante;
import com.donatrack.incentivos.domain.entities.RegistroDonacion;
import com.donatrack.incentivos.domain.entities.misiones.Mision;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.InsigniaEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.InsigniaObtenidaEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.MetricasDonanteEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.MisionEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.PerfilDonanteEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.ProgresoMisionEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.RegistroDonacionEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.mappers.PerfilDonanteMapper;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories.InsigniaJpaRepository;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories.MisionJpaRepository;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories.PerfilDonanteJpaRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class JpaPerfilDonanteRepository implements PerfilDonanteRepository {

  private final PerfilDonanteJpaRepository jpaRepository;
  private final InsigniaJpaRepository insigniaJpaRepository;
  private final MisionJpaRepository misionJpaRepository;

  public JpaPerfilDonanteRepository(PerfilDonanteJpaRepository jpaRepository) {
    this(jpaRepository, null, null);
  }

  @Autowired
  public JpaPerfilDonanteRepository(
      PerfilDonanteJpaRepository jpaRepository,
      InsigniaJpaRepository insigniaJpaRepository,
      MisionJpaRepository misionJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.insigniaJpaRepository = insigniaJpaRepository;
    this.misionJpaRepository = misionJpaRepository;
  }

  @Override
  public Optional<PerfilDonante> findById(UUID donanteId) {
    return jpaRepository.findById(donanteId).map(PerfilDonanteMapper::toDomain);
  }

  @Override
  @Transactional
  public void save(PerfilDonante perfil) {
    if (perfil == null || perfil.getDonanteId() == null) return;

    PerfilDonanteEntity entity =
        jpaRepository
            .findById(perfil.getDonanteId())
            .orElseGet(
                () -> {
                  PerfilDonanteEntity p = new PerfilDonanteEntity();
                  p.setPerfilDonanteId(perfil.getDonanteId());
                  return p;
                });

    if (perfil.getCategoria() != null) {
      entity.setCategoria(perfil.getCategoria().name());
    }

    entity.setMisionActual(resolveMision(perfil.getMisionActual()));

    // Insignias obtenidas
    entity.getInsigniasObtenidas().clear();
    if (perfil.getInsigniasObtenidas() != null) {
      for (Insignia ins : perfil.getInsigniasObtenidas()) {
        InsigniaEntity ie = resolveInsignia(ins);
        if (ie != null) {
          InsigniaObtenidaEntity ioe = new InsigniaObtenidaEntity();
          ioe.setId(UUID.randomUUID());
          ioe.setPerfil(entity);
          ioe.setInsignia(ie);
          ioe.setFechaObtencion(
              ins.getFechaObtencion() != null ? ins.getFechaObtencion() : LocalDate.now());
          ioe.setVisiblePublicamente(ins.isVisiblePublicamente());
          entity.getInsigniasObtenidas().add(ioe);
        }
      }
    }

    // Métricas del donante y registros de donación
    if (perfil.getMetricas() != null) {
      MetricasDonanteEntity mde = entity.getMetricas();
      if (mde == null) {
        mde = new MetricasDonanteEntity();
        mde.setId(UUID.randomUUID());
        mde.setPerfil(entity);
        entity.setMetricas(mde);
      }

      List<RegistroDonacion> regs =
          perfil.getMetricas().getRegistrosDonacion() != null
              ? perfil.getMetricas().getRegistrosDonacion()
              : Collections.emptyList();

      mde.setTotalDonacionesHistoricas(regs.size());
      int orgsAyudadas =
          (int)
              regs.stream()
                  .map(RegistroDonacion::getIdEntidadBeneficiaria)
                  .filter(Objects::nonNull)
                  .distinct()
                  .count();
      mde.setOrganizacionesAyudadas(orgsAyudadas);

      mde.getRegistrosDonacion().clear();
      for (RegistroDonacion reg : regs) {
        RegistroDonacionEntity rde = new RegistroDonacionEntity();
        rde.setId(UUID.randomUUID());
        rde.setMetricas(mde);
        rde.setIdDonacionOrigen(reg.getIdDonacion());
        rde.setCantidadBienes(reg.getCantidadBienes());
        rde.setCategorias(
            reg.getCategorias() != null ? String.join(",", reg.getCategorias()) : null);
        rde.setIdEntidadBeneficiariaOrigen(reg.getIdEntidadBeneficiaria());
        rde.setMesDonacion(reg.getMesDonacion() != null ? reg.getMesDonacion().toString() : null);
        mde.getRegistrosDonacion().add(rde);
      }

      // Progreso de misiones (completadas + misión actual en progreso)
      entity.getProgresosMisiones().clear();
      Map<Mision, YearMonth> completadas = perfil.getMetricas().getMisionesCompletadas();
      if (completadas != null) {
        for (Map.Entry<Mision, YearMonth> entry : completadas.entrySet()) {
          MisionEntity me = resolveMision(entry.getKey());
          if (me != null) {
            ProgresoMisionEntity pme = new ProgresoMisionEntity();
            pme.setId(UUID.randomUUID());
            pme.setPerfil(entity);
            pme.setMision(me);
            pme.setProgresoActual(
                me.getObjetivo() != null ? me.getObjetivo() : entry.getKey().getObjetivo());
            pme.setEstado("COMPLETADA");
            pme.setMesCompletada(entry.getValue() != null ? entry.getValue().toString() : null);
            entity.getProgresosMisiones().add(pme);
          }
        }
      }

      if (perfil.getMisionActual() != null
          && (completadas == null || !completadas.containsKey(perfil.getMisionActual()))) {
        MisionEntity meActual = entity.getMisionActual();
        if (meActual != null) {
          ProgresoMisionEntity pmeActual = new ProgresoMisionEntity();
          pmeActual.setId(UUID.randomUUID());
          pmeActual.setPerfil(entity);
          pmeActual.setMision(meActual);
          pmeActual.setProgresoActual(extraerProgresoNumerico(perfil.getMisionActual(), perfil));
          pmeActual.setEstado("EN_PROGRESO");
          entity.getProgresosMisiones().add(pmeActual);
        }
      }
    }

    jpaRepository.save(entity);
  }

  private int extraerProgresoNumerico(Mision mision, PerfilDonante perfil) {
    try {
      String texto = mision.getProgresoActual(perfil);
      if (texto != null && texto.contains("/")) {
        return Integer.parseInt(texto.split("/")[0].trim());
      }
    } catch (Exception ignored) {
    }
    return 0;
  }

  private InsigniaEntity resolveInsignia(Insignia ins) {
    if (ins == null) return null;
    if (insigniaJpaRepository == null) {
      InsigniaEntity ie = new InsigniaEntity();
      ie.setId(ins.getId() != null ? ins.getId() : UUID.randomUUID());
      ie.setNombre(ins.getNombre() != null ? ins.getNombre() : "Insignia");
      ie.setDescripcion(ins.getDescripcion());
      return ie;
    }

    if (ins.getId() != null) {
      Optional<InsigniaEntity> byId = insigniaJpaRepository.findById(ins.getId());
      if (byId.isPresent()) {
        return byId.get();
      }
    }
    if (ins.getNombre() != null && !ins.getNombre().isBlank()) {
      Optional<InsigniaEntity> byName =
          insigniaJpaRepository.findFirstByNombreIgnoreCase(ins.getNombre().trim());
      if (byName.isPresent()) {
        return byName.get();
      }
    }

    InsigniaEntity created = new InsigniaEntity();
    created.setNombre(ins.getNombre() != null ? ins.getNombre() : "Insignia");
    created.setDescripcion(ins.getDescripcion());
    return insigniaJpaRepository.save(created);
  }

  private MisionEntity resolveMision(Mision mision) {
    if (mision == null) return null;
    if (misionJpaRepository == null) {
      MisionEntity me = new MisionEntity();
      me.setId(UUID.randomUUID());
      me.setNombre(mision.getNombre() != null ? mision.getNombre() : "Mision");
      return me;
    }

    if (mision.getNombre() != null && !mision.getNombre().isBlank()) {
      Optional<MisionEntity> byName =
          misionJpaRepository.findFirstByNombreIgnoreCase(mision.getNombre().trim());
      if (byName.isPresent()) {
        return byName.get();
      }
    }

    MisionEntity created = new MisionEntity();
    created.setId(UUID.randomUUID());
    created.setNombre(mision.getNombre() != null ? mision.getNombre() : "Mision");
    if (mision.getTipoMetrica() != null) {
      created.setTipoMetrica(mision.getTipoMetrica().name());
    }
    created.setObjetivo(mision.getObjetivo());
    if (mision.getRecompensa() != null) {
      created.setRecompensaInsignia(resolveInsignia(mision.getRecompensa()));
    }
    return misionJpaRepository.save(created);
  }

  @Override
  public List<PerfilDonante> findAll() {
    return jpaRepository.findAll().stream()
        .map(PerfilDonanteMapper::toDomain)
        .collect(Collectors.toList());
  }
}
