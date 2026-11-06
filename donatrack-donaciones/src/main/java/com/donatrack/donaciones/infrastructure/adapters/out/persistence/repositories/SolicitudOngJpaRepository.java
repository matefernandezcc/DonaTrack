package com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories;

import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.SolicitudOngEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudOngJpaRepository extends JpaRepository<SolicitudOngEntity, UUID> {
  List<SolicitudOngEntity> findAllByOrderByFechaCreacionDesc();
}
