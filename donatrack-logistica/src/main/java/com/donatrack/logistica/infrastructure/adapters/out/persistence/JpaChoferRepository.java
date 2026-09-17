package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.ChoferRepositoryPort;
import com.donatrack.logistica.domain.entities.reparto.Chofer;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.ChoferEntity;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.ChoferMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.ChoferJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaChoferRepository implements ChoferRepositoryPort {

  private final ChoferJpaRepository jpaRepository;

  public JpaChoferRepository(ChoferJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public void guardar(Chofer chofer) {
    if (chofer == null) return;
    Optional<ChoferEntity> existing = jpaRepository.findByLegajo(chofer.getLegajo());
    ChoferEntity entity;
    if (existing.isPresent()) {
      entity = existing.get();
      entity.setNombre(chofer.getNombre());
    } else {
      entity = ChoferMapper.toEntity(chofer);
    }
    jpaRepository.save(entity);
  }

  @Override
  public Optional<Chofer> buscarPorLegajo(String legajo) {
    return jpaRepository.findByLegajo(legajo).map(ChoferMapper::toDomain);
  }

  @Override
  public List<Chofer> obtenerTodos() {
    return jpaRepository.findAll().stream().map(ChoferMapper::toDomain).toList();
  }

  @Override
  public void eliminar(String legajo) {
    jpaRepository.deleteByLegajo(legajo);
  }
}
