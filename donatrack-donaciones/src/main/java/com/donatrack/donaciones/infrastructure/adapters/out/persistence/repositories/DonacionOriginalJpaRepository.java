package com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories;

import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionOriginalEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DonacionOriginalJpaRepository
    extends JpaRepository<DonacionOriginalEntity, UUID> {

  @Query("SELECT DISTINCT d FROM DonacionOriginalEntity d JOIN d.donaciones seg WHERE seg.id = :idDonacion")
  Optional<DonacionOriginalEntity> findByDonacionId(@Param("idDonacion") UUID idDonacion);

  @Query(
      "SELECT seg.id, p.id, p.email, p.contactoCorreo "
          + "FROM DonacionOriginalEntity o JOIN o.donaciones seg JOIN o.donante don JOIN don.persona p")
  java.util.List<Object[]> findAllDonanteResumenPorDonacion();
}
