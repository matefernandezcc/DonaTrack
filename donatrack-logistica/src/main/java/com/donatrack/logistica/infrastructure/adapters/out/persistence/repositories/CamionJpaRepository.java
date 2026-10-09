package com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories;

import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.CamionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CamionJpaRepository extends JpaRepository<CamionEntity, UUID> {
  Optional<CamionEntity> findByPatente(String patente);

  void deleteByPatente(String patente);
}
