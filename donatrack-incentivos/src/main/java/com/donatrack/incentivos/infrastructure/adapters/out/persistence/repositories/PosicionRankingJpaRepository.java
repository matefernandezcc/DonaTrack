package com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories;

import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.PosicionRankingEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosicionRankingJpaRepository extends JpaRepository<PosicionRankingEntity, UUID> {}
