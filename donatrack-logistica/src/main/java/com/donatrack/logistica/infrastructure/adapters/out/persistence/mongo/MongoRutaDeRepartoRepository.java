package com.donatrack.logistica.infrastructure.adapters.out.persistence.mongo;

import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.EntregaDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ParadaDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.RutaDeRepartoDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo.RutaDeRepartoMongoMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo.EntregaMongoRepository;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo.RutaDeRepartoMongoRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class MongoRutaDeRepartoRepository implements RutaDeRepartoRepositoryPort {

  private final RutaDeRepartoMongoRepository mongoRepository;
  private final EntregaMongoRepository entregaMongoRepository;

  public MongoRutaDeRepartoRepository(RutaDeRepartoMongoRepository mongoRepository) {
    this(mongoRepository, null);
  }

  @Autowired
  public MongoRutaDeRepartoRepository(
      RutaDeRepartoMongoRepository mongoRepository,
      EntregaMongoRepository entregaMongoRepository) {
    this.mongoRepository = mongoRepository;
    this.entregaMongoRepository = entregaMongoRepository;
  }

  @Override
  public Optional<RutaDeReparto> buscarPorIdDonacion(UUID idDonacion) {
    return mongoRepository.findByEntregaId(idDonacion).stream()
        .findFirst()
        .map(RutaDeRepartoMongoMapper::toDomain);
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

    if (entregaMongoRepository != null && doc != null && doc.getParadas() != null) {
      for (ParadaDocument parada : doc.getParadas()) {
        if (parada.getEntregas() != null) {
          for (EntregaDocument ed : parada.getEntregas()) {
            UUID idEntrega = ed.getIdEntrega();
            if (idEntrega != null) {
              EntregaDocument standalone = new EntregaDocument();
              standalone.setIdEntrega(idEntrega);
              standalone.setEstado(ed.getEstado());
              standalone.setPesoEstimado(ed.getPesoEstimado());
              standalone.setVolumenEstimado(ed.getVolumenEstimado());
              standalone.setComprobanteRecepcion(ed.getComprobanteRecepcion());
              entregaMongoRepository.save(standalone);
            }
          }
        }
      }
    }
  }
}
