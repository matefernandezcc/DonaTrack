package com.donatrack.logistica.infrastructure.adapters.out.persistence.mongo;

import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.RutaDeRepartoDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo.RutaDeRepartoMongoMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo.RutaDeRepartoMongoRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class MongoRutaDeRepartoRepository implements RutaDeRepartoRepositoryPort {

  private final RutaDeRepartoMongoRepository mongoRepository;

  public MongoRutaDeRepartoRepository(RutaDeRepartoMongoRepository mongoRepository) {
    this.mongoRepository = mongoRepository;
  }

  @Override
  public Optional<RutaDeReparto> buscarPorIdDonacion(UUID idDonacion) {
    return mongoRepository.findByEntregaId(idDonacion).map(RutaDeRepartoMongoMapper::toDomain);
  }

  @Override
  public Optional<RutaDeReparto> buscarPorId(UUID idRuta) {
    return mongoRepository.findById(idRuta).map(RutaDeRepartoMongoMapper::toDomain);
  }

  @Override
  public List<RutaDeReparto> obtenerTodas() {
    return mongoRepository.findAll().stream().map(RutaDeRepartoMongoMapper::toDomain).toList();
  }

  @Override
  public void guardar(RutaDeReparto ruta) {
    RutaDeRepartoDocument doc = RutaDeRepartoMongoMapper.toDocument(ruta);
    mongoRepository.save(doc);
  }
}
