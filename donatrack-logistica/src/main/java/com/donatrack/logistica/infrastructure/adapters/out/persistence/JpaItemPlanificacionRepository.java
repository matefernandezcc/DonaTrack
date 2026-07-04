package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.ItemPlanificacionRepositoryPort;
import com.donatrack.logistica.domain.entities.planificacion.ItemPlanificacion;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.ItemPlanificacionMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.ItemPlanificacionJpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class JpaItemPlanificacionRepository implements ItemPlanificacionRepositoryPort {

  private final ItemPlanificacionJpaRepository jpaRepository;

  public JpaItemPlanificacionRepository(ItemPlanificacionJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public void guardar(ItemPlanificacion item) {
    jpaRepository.save(ItemPlanificacionMapper.toEntity(item));
  }

  @Override
  public List<ItemPlanificacion> obtenerTodos() {
    return jpaRepository.findAll().stream().map(ItemPlanificacionMapper::toDomain).toList();
  }
}
