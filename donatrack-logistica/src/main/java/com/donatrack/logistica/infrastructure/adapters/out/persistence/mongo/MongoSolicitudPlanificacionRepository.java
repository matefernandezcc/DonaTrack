package com.donatrack.logistica.infrastructure.adapters.out.persistence.mongo;

import com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepositoryPort;
import com.donatrack.logistica.domain.entities.planificacion.SolicitudPlanificacion;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.SolicitudPlanificacionDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo.SolicitudPlanificacionMongoMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo.SolicitudPlanificacionMongoRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class MongoSolicitudPlanificacionRepository implements SolicitudPlanificacionRepositoryPort {

  private final SolicitudPlanificacionMongoRepository mongoRepository;

  public MongoSolicitudPlanificacionRepository(
      SolicitudPlanificacionMongoRepository mongoRepository) {
    this.mongoRepository = mongoRepository;
  }

  @Override
  public void guardar(SolicitudPlanificacion solicitud) {
    SolicitudPlanificacionDocument doc = SolicitudPlanificacionMongoMapper.toDocument(solicitud);
    mongoRepository.save(doc);
  }

  @Override
  public Optional<SolicitudPlanificacion> buscarPorId(UUID id) {
    return mongoRepository.findById(id).map(SolicitudPlanificacionMongoMapper::toDomain);
  }
}
