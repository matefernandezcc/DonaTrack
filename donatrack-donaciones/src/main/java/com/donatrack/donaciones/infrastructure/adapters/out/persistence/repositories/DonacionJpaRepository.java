package com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories;

import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DonacionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DonacionJpaRepository extends JpaRepository<DonacionEntity, UUID> {

  List<DonacionEntity> findByEstado(String estado);

  @Query(
      value =
          "SELECT d FROM DonacionEntity d WHERE "
              + "(:estado IS NULL OR d.estado = :estado) AND "
              + "(:idDonante IS NULL OR d.donacionOriginal.donante.persona.id = :idDonante OR d.donacionOriginal.donante.id = :idDonante) AND "
              + "(:categoriaId IS NULL OR d.subcategoriaAsignada.categoria.id = :categoriaId)",
      countQuery =
          "SELECT count(d) FROM DonacionEntity d WHERE "
              + "(:estado IS NULL OR d.estado = :estado) AND "
              + "(:idDonante IS NULL OR d.donacionOriginal.donante.persona.id = :idDonante OR d.donacionOriginal.donante.id = :idDonante) AND "
              + "(:categoriaId IS NULL OR d.subcategoriaAsignada.categoria.id = :categoriaId)")
  Page<DonacionEntity> buscarConFiltrosPaginados(
      @Param("estado") String estado,
      @Param("idDonante") UUID idDonante,
      @Param("categoriaId") UUID categoriaId,
      Pageable pageable);

  @Query("SELECT d FROM DonacionEntity d LEFT JOIN FETCH d.bienes WHERE d.id = :id")
  Optional<DonacionEntity> buscarPorIdConBienes(@Param("id") UUID id);

  @Query(
      "SELECT count(d) FROM DonacionEntity d WHERE (d.donacionOriginal.donante.persona.id = :idDonante OR d.donacionOriginal.donante.id = :idDonante)")
  long contarPorDonante(@Param("idDonante") UUID idDonante);

  @Query(
      "SELECT count(d) FROM DonacionEntity d WHERE (d.donacionOriginal.donante.persona.id = :idDonante OR d.donacionOriginal.donante.id = :idDonante) AND d.estado = 'ENTREGADA'")
  long contarEntregadasPorDonante(@Param("idDonante") UUID idDonante);
}
