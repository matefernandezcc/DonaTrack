package com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo;

import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.CamionDocument;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CamionMongoRepository extends MongoRepository<CamionDocument, UUID> {
  Optional<CamionDocument> findByPatente(String patente);

  void deleteByPatente(String patente);
}
