package com.donatrack.logistica.infrastructure.adapters.out.persistence.mongo;

import com.donatrack.logistica.application.ports.out.EntregaRepositoryPort;
import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.EntregaDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ParadaDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.RutaDeRepartoDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo.RutaDeRepartoMongoMapper;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo.EntregaMongoRepository;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo.RutaDeRepartoMongoRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class MongoEntregaRepository implements EntregaRepositoryPort {

  private final RutaDeRepartoMongoRepository rutaRepository;
  private final EntregaMongoRepository entregaMongoRepository;

  public MongoEntregaRepository(
      RutaDeRepartoMongoRepository rutaRepository,
      EntregaMongoRepository entregaMongoRepository) {
    this.rutaRepository = rutaRepository;
    this.entregaMongoRepository = entregaMongoRepository;
  }

  @Override
  public Optional<Entrega> buscarPorId(UUID idEntrega) {
    if (idEntrega == null) return Optional.empty();

    // 1. Buscar en la ruta embebida (Aggregate Root)
    Optional<RutaDeRepartoDocument> rutaOpt = rutaRepository.findByEntregaId(idEntrega);
    if (rutaOpt.isPresent()) {
      for (ParadaDocument parada : rutaOpt.get().getParadas()) {
        if (parada.getEntregas() != null) {
          for (EntregaDocument ed : parada.getEntregas()) {
            if (idEntrega.equals(ed.getIdEntrega())) {
              return Optional.of(RutaDeRepartoMongoMapper.toEntregaDomain(ed));
            }
          }
        }
      }
    }

    // 2. Fallback a colección individual de entregas
    return entregaMongoRepository.findById(idEntrega).map(RutaDeRepartoMongoMapper::toEntregaDomain);
  }

  @Override
  public void guardar(Entrega entrega) {
    if (entrega == null || entrega.getIdEntrega() == null) return;

    EntregaDocument ed = RutaDeRepartoMongoMapper.toEntregaDocument(entrega);
    entregaMongoRepository.save(ed);

    // Si la entrega pertenece a una ruta, actualizar el subdocumento en la ruta
    Optional<RutaDeRepartoDocument> rutaOpt = rutaRepository.findByEntregaId(entrega.getIdEntrega());
    if (rutaOpt.isPresent()) {
      RutaDeRepartoDocument rutaDoc = rutaOpt.get();
      boolean modificado = false;
      for (ParadaDocument parada : rutaDoc.getParadas()) {
        if (parada.getEntregas() != null) {
          for (int i = 0; i < parada.getEntregas().size(); i++) {
            if (entrega.getIdEntrega().equals(parada.getEntregas().get(i).getIdEntrega())) {
              parada.getEntregas().set(i, ed);
              modificado = true;
              break;
            }
          }
        }
      }
      if (modificado) {
        rutaRepository.save(rutaDoc);
      }
    }
  }
}
