package com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories;

import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.RankingMensualEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RankingMensualJpaRepository extends JpaRepository<RankingMensualEntity, UUID> {
  Optional<RankingMensualEntity> findFirstByMesAndAnio(String mes, Integer anio);
}
