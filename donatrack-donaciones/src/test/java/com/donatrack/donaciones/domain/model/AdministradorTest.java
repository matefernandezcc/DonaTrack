package com.donatrack.donaciones.domain.model;

import org.junit.jupiter.api.Test;
import com.donatrack.donaciones.domain.model.persona.Coordenada;
import com.donatrack.donaciones.domain.model.persona.Direccion;
import com.donatrack.donaciones.domain.model.persona.Pais;
import com.donatrack.donaciones.domain.model.persona.Provincia;
import com.donatrack.donaciones.domain.model.roles.Administrador;

public class AdministradorTest {

  Pais Argentina = new Pais("Argentina", "Argentino");
  Provincia provinciaPrueba = new Provincia("Buenos Aires", Argentina);
  Coordenada coordenadaPrueba = new Coordenada(-34.6037, -58.3816);
  Direccion direccionPrueba =
      new Direccion(
          "calleFalsa", 123, "Springfield", provinciaPrueba, Argentina, "1234", coordenadaPrueba);

  Deposito depositoPrueba = new Deposito("depositoPrueba", 1000, direccionPrueba);

  @Test
  public void testErrorSinEstrategiaImportador() {

    Administrador admin = new Administrador(depositoPrueba);

    org.junit.jupiter.api.Assertions.assertFalse(admin.importarDonantesMasivos("rutaPrueba.csv"));
  }

  @Test
  public void testErrorSinEstrategiaAsignacion() {
    Administrador admin = new Administrador(depositoPrueba);

    org.junit.jupiter.api.Assertions.assertTrue(
        admin.obtenerRecomendacionAsignacion(null, null).isEmpty());
  }
}
