package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.CategoriaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.DireccionEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.PersonaHumanaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.SubcategoriaEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.CategoriaJpaRepository;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.PersonaJpaRepository;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.SubcategoriaJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class JpaPersistenceTest {

  @Autowired private CategoriaJpaRepository categoriaRepository;

  @Autowired private SubcategoriaJpaRepository subcategoriaRepository;

  @Autowired private PersonaJpaRepository personaRepository;

  @Test
  @DisplayName("Debe persistir y recuperar una Categoría y Subcategoría en base en memoria H2")
  void debePersistirCategoriaYSubcategoria() {
    CategoriaEntity cat = new CategoriaEntity();
    cat.setNombre("Ropa");
    cat.setDescripcion("Indumentaria variada");
    CategoriaEntity savedCat = categoriaRepository.save(cat);

    assertNotNull(savedCat.getId());

    SubcategoriaEntity sub = new SubcategoriaEntity();
    sub.setNombre("Camperas");
    sub.setDescripcion("Camperas de abrigo");
    sub.setCategoria(savedCat);
    SubcategoriaEntity savedSub = subcategoriaRepository.save(sub);

    assertNotNull(savedSub.getId());
    assertEquals(savedCat.getId(), savedSub.getCategoria().getId());
  }

  @Test
  @DisplayName("Debe persistir una Persona Humana con Dirección separada en base en memoria H2")
  void debePersistirPersonaHumanaConDireccion() {
    DireccionEntity dir = new DireccionEntity();
    dir.setCalle("Av. Corrientes");
    dir.setAltura(1234.0);
    dir.setLocalidad("CABA");

    PersonaHumanaEntity persona = new PersonaHumanaEntity();
    persona.setId(UUID.randomUUID());
    persona.setNombre("Juan");
    persona.setApellido("Perez");
    persona.setEdad(30);
    persona.setEmail("juan.perez@example.com");
    persona.setDireccion(dir);

    PersonaHumanaEntity savedPersona = personaRepository.save(persona);

    assertNotNull(savedPersona.getId());
    assertNotNull(savedPersona.getDireccion().getId());

    Optional<PersonaHumanaEntity> encontrada =
        personaRepository
            .findById(savedPersona.getId())
            .filter(p -> p instanceof PersonaHumanaEntity)
            .map(p -> (PersonaHumanaEntity) p);

    assertEquals(true, encontrada.isPresent());
    assertEquals("Juan", encontrada.get().getNombre());
    assertEquals("Av. Corrientes", encontrada.get().getDireccion().getCalle());
  }
}
