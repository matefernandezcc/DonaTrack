package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.ChoferRepositoryPort;
import com.donatrack.logistica.domain.entities.reparto.Chofer;
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
    jpaRepository.save(ChoferMapper.toEntity(chofer));
  }

  @Override
  public Optional<Chofer> buscarPorLegajo(String legajo) {
    return jpaRepository.findById(legajo).map(ChoferMapper::toDomain);
  }

  @Override
  public List<Chofer> obtenerTodos() {
    return jpaRepository.findAll().stream().map(ChoferMapper::toDomain).toList();
  }

  @Override
  public void eliminar(String legajo) {
    jpaRepository.deleteById(legajo);
  }
}
