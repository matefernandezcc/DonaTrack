package com.donatrack.donaciones.domain.model;

import org.junit.jupiter.api.Test;
import com.donatrack.donaciones.domain.model.roles.Administrador;

import java.util.UUID;

public class AdministradorTest {

  UUID idDepositoPrueba = UUID.randomUUID();

  @Test
  public void testErrorSinEstrategiaImportador() {
    Administrador admin = new Administrador(idDepositoPrueba);

    org.junit.jupiter.api.Assertions.assertFalse(admin.importarDonantesMasivos("rutaPrueba.csv"));
  }
}
