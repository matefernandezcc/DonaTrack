package com.donatrack.logistica.infrastructure.adapters.out.persistence.mongo;

import com.donatrack.logistica.application.ports.out.ChoferRepositoryPort;
import com.donatrack.logistica.domain.entities.reparto.Chofer;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ChoferDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo.ChoferMongoMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo.ChoferMongoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class MongoChoferRepository implements ChoferRepositoryPort {

  private final ChoferMongoRepository mongoRepository;

  public MongoChoferRepository(ChoferMongoRepository mongoRepository) {
    this.mongoRepository = mongoRepository;
  }

  @Override
  public void guardar(Chofer chofer) {
    Optional<ChoferDocument> existing = mongoRepository.findByLegajo(chofer.getLegajo());
    ChoferDocument doc = ChoferMongoMapper.toDocument(chofer);
    existing.ifPresent(e -> doc.setId(e.getId()));
    mongoRepository.save(doc);
  }

  @Override
  public Optional<Chofer> buscarPorLegajo(String legajo) {
    return mongoRepository.findByLegajo(legajo).map(ChoferMongoMapper::toDomain);
  }

  @Override
  public List<Chofer> obtenerTodos() {
    return mongoRepository.findAll().stream().map(ChoferMongoMapper::toDomain).toList();
  }

  @Override
  public void eliminar(String legajo) {
    mongoRepository.deleteByLegajo(legajo);
  }
}
