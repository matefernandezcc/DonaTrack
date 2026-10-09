package com.donatrack.notificaciones.infrastructure.adapters.out.persistence.repositories;

import com.donatrack.notificaciones.infrastructure.adapters.out.persistence.entities.NotificacionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionJpaRepository extends JpaRepository<NotificacionEntity, UUID> {}
