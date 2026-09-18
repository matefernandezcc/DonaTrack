package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.BeneficiarioRepository;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.BeneficiarioEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers.RolMapper;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.BeneficiarioJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class JpaBeneficiarioRepository implements BeneficiarioRepository {

  private final BeneficiarioJpaRepository jpaRepository;

  @PersistenceContext
  private EntityManager entityManager;

  public JpaBeneficiarioRepository(BeneficiarioJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public List<Beneficiario> buscarTodos() {
    return jpaRepository.findAll().stream()
        .map(entity -> (Beneficiario) RolMapper.toDomain(entity))
        .toList();
  }

  @Override
  public Optional<Beneficiario> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(entity -> (Beneficiario) RolMapper.toDomain(entity));
  }

  @Override
  @Transactional
  public Beneficiario guardar(Beneficiario beneficiario) {
    if (beneficiario.getFechaAlta() == null) {
      beneficiario.setFechaAlta(LocalDate.now());
    }
    BeneficiarioEntity entity = (BeneficiarioEntity) RolMapper.toEntity(beneficiario);
    if (entity.getId() == null || !jpaRepository.existsById(entity.getId())) {
      if (entity.getId() == null) {
        entity.setId(UUID.randomUUID());
      }
      entityManager.persist(entity);
    } else {
      entity = entityManager.merge(entity);
    }
    return (Beneficiario) RolMapper.toDomain(entity);
  }

  @Override
  @Transactional
  public void eliminarPorId(UUID id) {
    jpaRepository.deleteById(id);
  }
}
