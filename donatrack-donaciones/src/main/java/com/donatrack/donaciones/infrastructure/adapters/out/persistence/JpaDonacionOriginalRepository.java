package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.DonacionOriginalRepository;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers.DonacionOriginalMapper;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.DonacionOriginalJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaDonacionOriginalRepository implements DonacionOriginalRepository {

  private final DonacionOriginalJpaRepository jpaRepository;

  public JpaDonacionOriginalRepository(DonacionOriginalJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<DonacionOriginal> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(DonacionOriginalMapper::toDomain);
  }

  @Override
  public Optional<DonacionOriginal> buscarPorIdDonacion(UUID idDonacion) {
    // Para simplificar, buscamos todas y filtramos, o se debería agregar el query en el repo JPA
    return jpaRepository.findAll().stream()
        .filter(d -> d.getDonaciones().stream().anyMatch(seg -> seg.getId().equals(idDonacion)))
        .map(DonacionOriginalMapper::toDomain)
        .findFirst();
  }

  @Override
  public void guardar(DonacionOriginal donacionOriginal) {
    jpaRepository.save(DonacionOriginalMapper.toEntity(donacionOriginal));
  }
}
