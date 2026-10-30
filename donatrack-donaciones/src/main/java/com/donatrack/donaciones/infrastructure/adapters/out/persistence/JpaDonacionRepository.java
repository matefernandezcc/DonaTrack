package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.HistorialEstado;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.HistorialEstadoEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.NecesidadEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers.DonacionMapper;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.BeneficiarioJpaRepository;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.DonacionJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class JpaDonacionRepository implements DonacionRepository {

  private final DonacionJpaRepository jpaRepository;
  private final BeneficiarioJpaRepository beneficiarioJpaRepository;

  public JpaDonacionRepository(DonacionJpaRepository jpaRepository) {
    this(jpaRepository, null);
  }

  @Autowired
  public JpaDonacionRepository(
      DonacionJpaRepository jpaRepository, BeneficiarioJpaRepository beneficiarioJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.beneficiarioJpaRepository = beneficiarioJpaRepository;
  }

  @Override
  public List<Donacion> buscarPorEstado(EstadoDonacion estado) {
    return jpaRepository.findByEstado(estado.name()).stream()
        .map(DonacionMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<Donacion> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(DonacionMapper::toDomain);
  }

  @Override
  @Transactional
  public void guardar(Donacion donacion) {
    if (donacion == null) return;

    Optional<DonacionEntity> existenteOpt =
        donacion.getId() != null ? jpaRepository.findById(donacion.getId()) : Optional.empty();

    if (existenteOpt.isPresent()) {
      DonacionEntity entity = existenteOpt.get();
      if (donacion.getEstado() != null) {
        entity.setEstado(donacion.getEstado().name());
      }

      if (donacion.getEntidadAsignada() != null
          && donacion.getEntidadAsignada().getId() != null
          && beneficiarioJpaRepository != null) {
        beneficiarioJpaRepository
            .findById(donacion.getEntidadAsignada().getId())
            .ifPresent(
                benefEntity -> {
                  NecesidadEntity necesidadElegida = null;
                  if (benefEntity.getNecesidadesDeclaradas() != null
                      && !benefEntity.getNecesidadesDeclaradas().isEmpty()) {
                    if (entity.getSubcategoriaAsignada() != null) {
                      necesidadElegida =
                          benefEntity.getNecesidadesDeclaradas().stream()
                              .filter(
                                  n ->
                                      n.getSubcategoriaRequerida() != null
                                          && n.getSubcategoriaRequerida()
                                              .getId()
                                              .equals(entity.getSubcategoriaAsignada().getId()))
                              .findFirst()
                              .orElse(benefEntity.getNecesidadesDeclaradas().get(0));
                    } else {
                      necesidadElegida = benefEntity.getNecesidadesDeclaradas().get(0);
                    }
                  }
                  if (necesidadElegida != null) {
                    entity.setNecesidad(necesidadElegida);
                  }
                });
      }

      if (donacion.getHistorial() != null) {
        int existentesCount =
            entity.getHistorialEstados() != null ? entity.getHistorialEstados().size() : 0;
        for (int i = existentesCount; i < donacion.getHistorial().size(); i++) {
          HistorialEstado h = donacion.getHistorial().get(i);
          HistorialEstadoEntity he = new HistorialEstadoEntity();
          he.setDonacion(entity);
          if (h.getEstado() != null) {
            he.setEstado(h.getEstado().name());
          }
          he.setFecha(LocalDateTime.now());
          he.setObservacion(h.getObservacion());
          he.setUsuarioId(h.getUsuarioId());
          entity.getHistorialEstados().add(he);
        }
      }

      jpaRepository.save(entity);
    } else {
      jpaRepository.save(DonacionMapper.toEntity(donacion));
    }
  }
}
