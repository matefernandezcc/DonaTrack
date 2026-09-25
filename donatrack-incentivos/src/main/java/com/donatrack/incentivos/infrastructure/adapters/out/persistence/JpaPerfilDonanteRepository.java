package com.donatrack.incentivos.infrastructure.adapters.out.persistence;

import com.donatrack.incentivos.application.ports.out.PerfilDonanteRepository;
import com.donatrack.incentivos.domain.entities.PerfilDonante;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.PerfilDonanteEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.mappers.PerfilDonanteMapper;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories.PerfilDonanteJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class JpaPerfilDonanteRepository implements PerfilDonanteRepository {

  private final PerfilDonanteJpaRepository jpaRepository;

  public JpaPerfilDonanteRepository(PerfilDonanteJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<PerfilDonante> findById(UUID donanteId) {
    return jpaRepository.findById(donanteId).map(PerfilDonanteMapper::toDomain);
  }

  @Override
  @Transactional
  public void save(PerfilDonante perfil) {
    if (perfil == null) return;
    PerfilDonanteEntity entity = PerfilDonanteMapper.toEntity(perfil);
    jpaRepository.save(entity);
  }

  @Override
  public List<PerfilDonante> findAll() {
    return jpaRepository.findAll().stream()
        .map(PerfilDonanteMapper::toDomain)
        .collect(Collectors.toList());
  }
}
