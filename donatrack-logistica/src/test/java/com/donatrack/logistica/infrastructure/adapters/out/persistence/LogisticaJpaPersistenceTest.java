package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.CamionEntity;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.entities.ChoferEntity;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.CamionJpaRepository;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.repositories.ChoferJpaRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class LogisticaJpaPersistenceTest {

  @Autowired private CamionJpaRepository camionRepository;

  @Autowired private ChoferJpaRepository choferRepository;

  @Test
  @DisplayName("Debe persistir un Camión con UUID generado automáticamente en H2")
  void debePersistirCamion() {
    CamionEntity camion = new CamionEntity();
    camion.setPatente("AA123BB");
    camion.setCapacidadCarga(3500.0);
    camion.setCapacidadVolumen(15.5);
    camion.setAltura(2.8);

    CamionEntity guardado = camionRepository.save(camion);

    assertNotNull(guardado.getId());
    Optional<CamionEntity> encontrado = camionRepository.findByPatente("AA123BB");
    assertEquals(true, encontrado.isPresent());
    assertEquals(3500.0, encontrado.get().getCapacidadCarga());
    assertEquals(15.5, encontrado.get().getCapacidadVolumen());
  }

  @Test
  @DisplayName("Debe persistir un Chofer con UUID y legajo único en H2")
  void debePersistirChofer() {
    ChoferEntity chofer = new ChoferEntity();
    chofer.setLegajo("CH-9021");
    chofer.setNombre("Carlos Gonzalez");

    ChoferEntity guardado = choferRepository.save(chofer);

    assertNotNull(guardado.getId());
    Optional<ChoferEntity> encontrado = choferRepository.findByLegajo("CH-9021");
    assertEquals(true, encontrado.isPresent());
    assertEquals("Carlos Gonzalez", encontrado.get().getNombre());
  }
}
