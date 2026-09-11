package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.EntregaRepositoryPort;
import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.EntregaMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.EntregaJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaEntregaRepository implements EntregaRepositoryPort {

  private final EntregaJpaRepository jpaRepository;

  public JpaEntregaRepository(EntregaJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<Entrega> buscarPorId(UUID idEntrega) {
    return jpaRepository.findById(idEntrega).map(EntregaMapper::toDomain);
  }

  @Override
  public void guardar(Entrega entrega) {
    jpaRepository.save(EntregaMapper.toEntity(entrega));
  }
}
