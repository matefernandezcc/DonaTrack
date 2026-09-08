package com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories;

import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.RutaDeRepartoEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaDeRepartoJpaRepository extends JpaRepository<RutaDeRepartoEntity, UUID> {

  /** Busca la ruta que contiene una entrega con el ID de donación dado. */
  Optional<RutaDeRepartoEntity> findByParadas_Entregas_IdEntrega(UUID idEntrega);
}
