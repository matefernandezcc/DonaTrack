package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.DonacionOriginalRepository;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionOriginalEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonanteEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers.DonacionOriginalMapper;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.DonacionOriginalJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class JpaDonacionOriginalRepository implements DonacionOriginalRepository {

  private final DonacionOriginalJpaRepository jpaRepository;

  @PersistenceContext private EntityManager entityManager;

  public JpaDonacionOriginalRepository(DonacionOriginalJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<DonacionOriginal> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(DonacionOriginalMapper::toDomain);
  }

  @Override
  public Optional<DonacionOriginal> buscarPorIdDonacion(UUID idDonacion) {
    return jpaRepository.findAll().stream()
        .filter(d -> d.getDonaciones().stream().anyMatch(seg -> seg.getId().equals(idDonacion)))
        .map(DonacionOriginalMapper::toDomain)
        .findFirst();
  }

  @Override
  @Transactional
  public void guardar(DonacionOriginal donacionOriginal) {
    DonacionOriginalEntity entity = DonacionOriginalMapper.toEntity(donacionOriginal);

    // Asignar donante con referencia manejada por Hibernate (evita entidad detached)
    if (donacionOriginal.getDonante() != null && donacionOriginal.getDonante().getId() != null) {
      DonanteEntity donanteRef =
          entityManager.getReference(DonanteEntity.class, donacionOriginal.getDonante().getId());
      entity.setDonante(donanteRef);
    }

    entityManager.persist(entity);
  }
}
