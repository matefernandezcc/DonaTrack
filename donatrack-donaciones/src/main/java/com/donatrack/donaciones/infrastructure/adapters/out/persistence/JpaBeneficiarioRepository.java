package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.BeneficiarioRepository;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers.RolMapper;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.BeneficiarioJpaRepository;
import java.util.List;
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
}
