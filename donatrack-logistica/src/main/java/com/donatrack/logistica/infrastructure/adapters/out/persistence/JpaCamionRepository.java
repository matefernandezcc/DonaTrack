package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.CamionRepositoryPort;
import com.donatrack.logistica.domain.entities.reparto.Camion;
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
    jpaRepository.save(CamionMapper.toEntity(camion));
  }

  @Override
  public Optional<Camion> buscarPorPatente(String patente) {
    return jpaRepository.findById(patente).map(CamionMapper::toDomain);
  }

  @Override
  public List<Camion> obtenerTodos() {
    return jpaRepository.findAll().stream().map(CamionMapper::toDomain).toList();
  }

  @Override
  public void eliminar(String patente) {
    jpaRepository.deleteById(patente);
  }
}
