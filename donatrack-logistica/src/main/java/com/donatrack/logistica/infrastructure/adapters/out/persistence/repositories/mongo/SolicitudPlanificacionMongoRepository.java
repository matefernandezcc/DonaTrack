package com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.mongo;

import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.SolicitudPlanificacionDocument;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudPlanificacionMongoRepository
    extends MongoRepository<SolicitudPlanificacionDocument, UUID> {}
