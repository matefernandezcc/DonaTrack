package com.donatrack.donaciones.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.donatrack.donaciones.domain.entities.enums.MedioContacto;
import com.donatrack.donaciones.domain.entities.enums.TipoDocumento;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.DocumentoIdentidad;
import com.donatrack.donaciones.domain.entities.persona.PersonaHumana;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.*;
import com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.importador.ImportadorCSV;
import org.junit.jupiter.api.Test;


public class ImportadorCSVTest {

  Pais Argentina = new Pais("Argentina", "Argentino");
  Provincia provinciaPrueba = new Provincia("Buenos Aires", Argentina);
  Coordenada coordenadaPrueba = new Coordenada(-34.6037, -58.3816);
  Direccion direccionPrueba = new Direccion(
      "calleFalsa", 123, "Springfield", provinciaPrueba, "1234", coordenadaPrueba);
  DocumentoIdentidad documentoPrueba = new DocumentoIdentidad(TipoDocumento.DNI, "43637832");

  PersonaHumana personaPrueba = new PersonaHumana(
      "emailprueba@prueba.com",
      new Contacto("emailprueba@prueba.com", "123456789", "987654321", MedioContacto.CORREO),
      direccionPrueba,
      documentoPrueba,
      "Dario",
      "Dardo",
      23);

  @Test
  public void testImportar() {
    PersonaHumana personaPrueba2 = new PersonaHumana(
        "emailABuscar@prueba.com",
        new Contacto("emailABuscar@prueba.com", "123456789", "987654321", MedioContacto.CORREO),
        direccionPrueba,
        documentoPrueba,
        "Roberto",
        "Carlos",
        56);

    com.donatrack.donaciones.application.ports.out.PersonaRepository personaRepository = new com.donatrack.donaciones.infrastructure.adapters.out.persistence.MockPersonaRepository();

    ImportadorCSV importador = new ImportadorCSV(personaRepository);

    byte[] contenido;
    try {
        contenido = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("../enunciado/CSV/donantes.csv"));
    } catch (java.io.IOException e) {
        contenido = new byte[0];
    }
    importador.importar(new com.donatrack.donaciones.domain.entities.donacion.Archivo("donantes.csv", contenido));

    assertEquals(19988, importador.getRegistroPersonas().size());
  }
}
