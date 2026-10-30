package com.donatrack.logistica.infrastructure.adapters.out.persistence.mongo;

import com.donatrack.logistica.application.ports.out.ItemPlanificacionRepositoryPort;
import com.donatrack.logistica.domain.entities.planificacion.ItemPlanificacion;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ItemPlanificacionDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo.ItemPlanificacionMongoMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo.ItemPlanificacionMongoRepository;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class MongoItemPlanificacionRepository implements ItemPlanificacionRepositoryPort {

  private final ItemPlanificacionMongoRepository mongoRepository;

  public MongoItemPlanificacionRepository(ItemPlanificacionMongoRepository mongoRepository) {
    this.mongoRepository = mongoRepository;
  }

  @Override
  public void guardar(ItemPlanificacion item) {
    ItemPlanificacionDocument doc = ItemPlanificacionMongoMapper.toDocument(item);
    mongoRepository.save(doc);
  }

  @Override
  public List<ItemPlanificacion> obtenerTodos() {
    return mongoRepository.findAll().stream().map(ItemPlanificacionMongoMapper::toDomain).toList();
  }

  @Override
  public void eliminarTodos(List<ItemPlanificacion> items) {
    mongoRepository.deleteAll();
  }
}
