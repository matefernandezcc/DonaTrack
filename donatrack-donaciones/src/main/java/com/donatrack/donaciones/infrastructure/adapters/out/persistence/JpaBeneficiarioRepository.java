package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.BeneficiarioRepository;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.BeneficiarioEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers.RolMapper;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.BeneficiarioJpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaBeneficiarioRepository implements BeneficiarioRepository {

  private final BeneficiarioJpaRepository jpaRepository;

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
  public Beneficiario guardar(Beneficiario beneficiario) {
    if (beneficiario.getFechaAlta() == null) {
      beneficiario.setFechaAlta(LocalDate.now());
    }
    BeneficiarioEntity entity = (BeneficiarioEntity) RolMapper.toEntity(beneficiario);
    BeneficiarioEntity saved = jpaRepository.save(entity);
    return (Beneficiario) RolMapper.toDomain(saved);
  }

  @Override
  public void eliminarPorId(UUID id) {
    jpaRepository.deleteById(id);
  }
}
