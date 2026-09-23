package com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories;

import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.ChoferEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoferJpaRepository extends JpaRepository<ChoferEntity, UUID> {
  Optional<ChoferEntity> findByLegajo(String legajo);
  void deleteByLegajo(String legajo);
}
