package com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo;

import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ChoferDocument;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoferMongoRepository extends MongoRepository<ChoferDocument, UUID> {
  Optional<ChoferDocument> findByLegajo(String legajo);

  void deleteByLegajo(String legajo);
}
