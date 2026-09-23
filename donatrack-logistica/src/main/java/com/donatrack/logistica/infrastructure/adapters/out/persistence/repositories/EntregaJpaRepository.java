package com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories;

import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.EntregaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntregaJpaRepository extends JpaRepository<EntregaEntity, UUID> {
}
