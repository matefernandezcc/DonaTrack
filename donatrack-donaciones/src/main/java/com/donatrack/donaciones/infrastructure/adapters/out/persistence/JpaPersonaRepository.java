package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.mappers.PersonaMapper;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.PersonaJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaPersonaRepository implements PersonaRepository {

  private final PersonaJpaRepository jpaRepository;

  public JpaPersonaRepository(PersonaJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<Persona> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(PersonaMapper::toDomain);
  }

  @Override
  public Optional<Persona> buscarPorEmail(String email) {
    return jpaRepository.findAll().stream()
        .filter(p -> email.equals(p.getEmail()) || email.equals(p.getContactoCorreo()))
        .findFirst()
        .map(PersonaMapper::toDomain);
  }

  @Override
  public List<Persona> obtenerTodas() {
    return jpaRepository.findAll().stream().map(PersonaMapper::toDomain).toList();
  }

  @Override
  public void guardar(Persona persona) {
    jpaRepository.save(PersonaMapper.toEntity(persona));
  }

  @Override
  public Optional<Persona> buscarPorRolId(UUID rolId) {
    return jpaRepository.findAll().stream()
        .filter(p -> p.getRoles() != null && p.getRoles().stream().anyMatch(r -> r.getId().equals(rolId)))
        .findFirst()
        .map(PersonaMapper::toDomain);
  }
}
