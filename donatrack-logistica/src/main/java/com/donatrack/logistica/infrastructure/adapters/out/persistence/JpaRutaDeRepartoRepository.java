package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.RutaDeRepartoMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.RutaDeRepartoJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaRutaDeRepartoRepository implements RutaDeRepartoRepositoryPort {

  private final RutaDeRepartoJpaRepository jpaRepository;

  public JpaRutaDeRepartoRepository(RutaDeRepartoJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<RutaDeReparto> buscarPorIdDonacion(UUID idDonacion) {
    return jpaRepository
        .findByParadas_Entregas_IdEntrega(idDonacion)
        .map(RutaDeRepartoMapper::toDomain);
  }

  @Override
  public Optional<RutaDeReparto> buscarPorId(UUID idRuta) {
    return jpaRepository.findById(idRuta).map(RutaDeRepartoMapper::toDomain);
  }

  @Override
  public List<RutaDeReparto> obtenerTodas() {
    return jpaRepository.findAll().stream().map(RutaDeRepartoMapper::toDomain).toList();
  }

  @Override
  public void guardar(RutaDeReparto ruta) {
    jpaRepository.save(RutaDeRepartoMapper.toEntity(ruta));
  }
}
