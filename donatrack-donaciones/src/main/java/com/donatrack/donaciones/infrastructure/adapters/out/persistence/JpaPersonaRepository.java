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
    // Necesitaremos un query method en el repo, pero por ahora lo buscamos iterando o podemos agregarlo.
    // Lo agrego filtrando por ahora, asumiendo que lo agregaremos al JpaRepository.
    return jpaRepository.findAll().stream()
        .filter(p -> p.getContacto() != null && email.equals(p.getContacto().getCorreo()))
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
        .filter(p -> p.getRoles().stream().anyMatch(r -> r.getId().equals(rolId)))
        .findFirst()
        .map(PersonaMapper::toDomain);
  }
}
