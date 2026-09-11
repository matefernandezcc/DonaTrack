package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.BeneficiarioRepository;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers.RolMapper;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.BeneficiarioJpaRepository;
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
    return jpaRepository.findById(id)
        .map(entity -> (Beneficiario) RolMapper.toDomain(entity));
  }

  @Override
  public void guardar(Beneficiario beneficiario) {
    jpaRepository.save(
        (com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.BeneficiarioEntity)
            RolMapper.toEntity(beneficiario));
  }

  @Override
  public void eliminar(UUID id) {
    jpaRepository.deleteById(id);
  }
}
