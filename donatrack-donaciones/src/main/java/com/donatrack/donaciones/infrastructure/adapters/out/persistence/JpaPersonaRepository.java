package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.PersonaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers.PersonaMapper;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.PersonaJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class JpaPersonaRepository implements PersonaRepository {

  private final PersonaJpaRepository jpaRepository;

  @PersistenceContext private EntityManager entityManager;

  public JpaPersonaRepository(PersonaJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<Persona> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(PersonaMapper::toDomain);
  }

  @Override
  public Optional<Persona> buscarPorEmail(String email) {
    return jpaRepository.findByEmail(email).map(PersonaMapper::toDomain);
  }

  @Override
  public List<Persona> obtenerTodas() {
    return jpaRepository.findAll().stream().map(PersonaMapper::toDomain).toList();
  }

  @Override
  @Transactional
  public void guardar(Persona persona) {
    PersonaEntity entity = PersonaMapper.toEntity(persona);
    if (entity.getId() == null || !jpaRepository.existsById(entity.getId())) {
      if (entity.getId() == null) {
        entity.setId(UUID.randomUUID());
      }
      entityManager.persist(entity);
    } else {
      entityManager.merge(entity);
    }
  }

  @Override
  public Optional<Persona> buscarPorRolId(UUID rolId) {
    return jpaRepository.findAll().stream()
        .filter(
            p ->
                p.getRoles() != null
                    && p.getRoles().stream().anyMatch(r -> r.getId().equals(rolId)))
        .findFirst()
        .map(PersonaMapper::toDomain);
  }
}
