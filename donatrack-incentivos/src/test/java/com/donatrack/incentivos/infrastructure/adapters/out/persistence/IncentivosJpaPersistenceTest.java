package com.donatrack.incentivos.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.InsigniaEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.PerfilDonanteEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories.InsigniaJpaRepository;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.repositories.PerfilDonanteJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class IncentivosJpaPersistenceTest {

  @Autowired private InsigniaJpaRepository insigniaRepository;

  @Autowired private PerfilDonanteJpaRepository perfilDonanteRepository;

  @Test
  @DisplayName("Debe persistir y recuperar una Insignia en H2 in-memory")
  void debePersistirInsignia() {
    InsigniaEntity insignia = new InsigniaEntity();
    insignia.setNombre("Primer Aporte");
    insignia.setDescripcion("Otorgada por realizar la primera donación");

    InsigniaEntity guardada = insigniaRepository.save(insignia);

    assertNotNull(guardada.getId());
    Optional<InsigniaEntity> buscada = insigniaRepository.findById(guardada.getId());
    assertEquals(true, buscada.isPresent());
    assertEquals("Primer Aporte", buscada.get().getNombre());
  }

  @Test
  @DisplayName("Debe persistir un Perfil de Donante en H2 in-memory")
  void debePersistirPerfilDonante() {
    UUID id = UUID.randomUUID();
    PerfilDonanteEntity perfil = new PerfilDonanteEntity();
    perfil.setPerfilDonanteId(id);
    perfil.setCategoria("BRONCE");

    PerfilDonanteEntity guardado = perfilDonanteRepository.save(perfil);

    assertNotNull(guardado.getPerfilDonanteId());
    assertEquals("BRONCE", guardado.getCategoria());
  }
}
