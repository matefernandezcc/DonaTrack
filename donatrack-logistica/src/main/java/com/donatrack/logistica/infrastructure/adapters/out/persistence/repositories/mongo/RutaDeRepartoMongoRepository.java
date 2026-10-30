package com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo;

import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.RutaDeRepartoDocument;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaDeRepartoMongoRepository extends MongoRepository<RutaDeRepartoDocument, UUID> {

  @Query("{ 'paradas.entregas.idEntrega': ?0 }")
  List<RutaDeRepartoDocument> findByEntregaId(UUID idEntrega);

  List<RutaDeRepartoDocument> findByChoferId(UUID choferId);

  List<RutaDeRepartoDocument> findByFechaOperativa(LocalDate fechaOperativa);
}
