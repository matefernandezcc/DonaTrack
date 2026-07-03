package com.donatrack.donaciones.domain.entities;

import org.junit.jupiter.api.Test;
import com.donatrack.donaciones.domain.entities.roles.Administrador;

import com.donatrack.donaciones.domain.entities.donacion.Archivo;
import java.util.UUID;

public class AdministradorTest {

  UUID idDepositoPrueba = UUID.randomUUID();

  @Test
  public void testErrorSinEstrategiaImportador() {
    Administrador admin = new Administrador(idDepositoPrueba);

    org.junit.jupiter.api.Assertions.assertFalse(admin.importarDonantesMasivos(new Archivo("rutaPrueba.csv", new byte[0])));
  }
}
