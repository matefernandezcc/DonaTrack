package com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories;

import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.RutaDeRepartoEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaDeRepartoJpaRepository extends JpaRepository<RutaDeRepartoEntity, UUID> {

  /**
   * Busca la ruta que contiene una entrega con el ID de donación dado.
   *
   * <p>Se usa DISTINCT porque el JOIN paradas→entregas puede generar filas duplicadas de la misma
   * ruta cuando una donación aparece en más de una parada/entrega, lo que causaba
   * {@code NonUniqueResultException} con el query derivado anterior.
   */
  @Query(
      "SELECT DISTINCT r FROM RutaDeRepartoEntity r "
          + "JOIN r.paradas p "
          + "JOIN p.entregas e "
          + "WHERE e.idDonacion = :idDonacion")
  List<RutaDeRepartoEntity> findByEntregaIdDonacion(@Param("idDonacion") UUID idDonacion);

  /** Wrapper que devuelve Optional con el primer resultado. */
  default Optional<RutaDeRepartoEntity> findFirstByEntregaIdDonacion(UUID idDonacion) {
    List<RutaDeRepartoEntity> results = findByEntregaIdDonacion(idDonacion);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }
}
