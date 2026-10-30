package com.donatrack.logistica.infrastructure.adapters.out.persistence.mongo;

import com.donatrack.logistica.application.ports.out.CamionRepositoryPort;
import com.donatrack.logistica.domain.entities.reparto.Camion;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.CamionDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo.CamionMongoMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo.CamionMongoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class MongoCamionRepository implements CamionRepositoryPort {

  private final CamionMongoRepository mongoRepository;

  public MongoCamionRepository(CamionMongoRepository mongoRepository) {
    this.mongoRepository = mongoRepository;
  }

  @Override
  public void guardar(Camion camion) {
    Optional<CamionDocument> existing = mongoRepository.findByPatente(camion.getPatente());
    CamionDocument doc = CamionMongoMapper.toDocument(camion);
    existing.ifPresent(e -> doc.setId(e.getId()));
    mongoRepository.save(doc);
  }

  @Override
  public Optional<Camion> buscarPorPatente(String patente) {
    return mongoRepository.findByPatente(patente).map(CamionMongoMapper::toDomain);
  }

  @Override
  public List<Camion> obtenerTodos() {
    return mongoRepository.findAll().stream().map(CamionMongoMapper::toDomain).toList();
  }

  @Override
  public void eliminar(String patente) {
    mongoRepository.deleteByPatente(patente);
  }
}
