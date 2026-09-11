package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers.DonacionMapper;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.DonacionJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaDonacionRepository implements DonacionRepository {

  private final DonacionJpaRepository jpaRepository;

  public JpaDonacionRepository(DonacionJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public List<Donacion> buscarPorEstado(EstadoDonacion estado) {
    return jpaRepository.findByEstado(estado.name()).stream()
        .map(DonacionMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<Donacion> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(DonacionMapper::toDomain);
  }

  @Override
  public void guardar(Donacion donacion) {
    jpaRepository.save(DonacionMapper.toEntity(donacion));
  }

  @Override
  public void eliminar(UUID id) {
    jpaRepository.deleteById(id);
  }
}
