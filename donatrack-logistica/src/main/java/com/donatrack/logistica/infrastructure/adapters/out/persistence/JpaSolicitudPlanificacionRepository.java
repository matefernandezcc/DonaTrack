package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepositoryPort;
import com.donatrack.logistica.domain.entities.planificacion.SolicitudPlanificacion;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.SolicitudPlanificacionMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.SolicitudPlanificacionJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaSolicitudPlanificacionRepository implements SolicitudPlanificacionRepositoryPort {

  private final SolicitudPlanificacionJpaRepository jpaRepository;

  public JpaSolicitudPlanificacionRepository(SolicitudPlanificacionJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public void guardar(SolicitudPlanificacion solicitud) {
    jpaRepository.save(SolicitudPlanificacionMapper.toEntity(solicitud));
  }

  @Override
  public Optional<SolicitudPlanificacion> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(SolicitudPlanificacionMapper::toDomain);
  }
}
