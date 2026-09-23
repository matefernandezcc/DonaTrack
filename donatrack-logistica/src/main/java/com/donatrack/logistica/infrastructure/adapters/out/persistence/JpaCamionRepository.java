package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.CamionRepositoryPort;
import com.donatrack.logistica.domain.entities.reparto.Camion;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.CamionEntity;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.CamionMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.CamionJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCamionRepository implements CamionRepositoryPort {

  private final CamionJpaRepository jpaRepository;

  public JpaCamionRepository(CamionJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public void guardar(Camion camion) {
    if (camion == null) return;
    Optional<CamionEntity> existing = jpaRepository.findByPatente(camion.getPatente());
    CamionEntity entity;
    if (existing.isPresent()) {
      entity = existing.get();
      entity.setCapacidadVolumen(camion.getCapacidadVolumen());
      entity.setAltura(camion.getAltura());
      entity.setCapacidadCarga(camion.getCapacidadCarga());
    } else {
      entity = CamionMapper.toEntity(camion);
    }
    jpaRepository.save(entity);
  }

  @Override
  public Optional<Camion> buscarPorPatente(String patente) {
    return jpaRepository.findByPatente(patente).map(CamionMapper::toDomain);
  }

  @Override
  public List<Camion> obtenerTodos() {
    return jpaRepository.findAll().stream().map(CamionMapper::toDomain).toList();
  }

  @Override
  public void eliminar(String patente) {
    jpaRepository.deleteByPatente(patente);
  }
}
