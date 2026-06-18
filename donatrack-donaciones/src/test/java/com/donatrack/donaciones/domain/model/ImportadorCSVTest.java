package com.donatrack.donaciones.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.donatrack.donaciones.domain.model.enums.MedioContacto;
import com.donatrack.donaciones.domain.model.enums.TipoDocumento;
import com.donatrack.donaciones.domain.model.persona.Contacto;
import com.donatrack.donaciones.domain.model.persona.DocumentoIdentidad;
import com.donatrack.donaciones.domain.model.persona.Persona;
import com.donatrack.donaciones.domain.model.persona.PersonaHumana;
import com.donatrack.donaciones.domain.model.persona.ubicacion.*;
import com.donatrack.donaciones.domain.model.roles.strategyAdministrador.importador.ImportadorCSV;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

;

public class ImportadorCSVTest {

  Pais Argentina = new Pais("Argentina", "Argentino");
  Provincia provinciaPrueba = new Provincia("Buenos Aires", Argentina);
  Coordenada coordenadaPrueba = new Coordenada(-34.6037, -58.3816);
  Direccion direccionPrueba =
      new Direccion(
          "calleFalsa", 123, "Springfield", provinciaPrueba, "1234", coordenadaPrueba);
  DocumentoIdentidad documentoPrueba = new DocumentoIdentidad(TipoDocumento.DNI, "43637832");

  PersonaHumana personaPrueba =
      new PersonaHumana(
          "emailprueba@prueba.com",
          new Contacto("emailprueba@prueba.com", "123456789", "987654321", MedioContacto.CORREO),
          direccionPrueba,
          documentoPrueba,
          "Dario",
          "Dardo",
          23);

  @Test
  public void testImportar() {
    PersonaHumana personaPrueba2 =
        new PersonaHumana(
            "emailABuscar@prueba.com",
            new Contacto("emailABuscar@prueba.com", "123456789", "987654321", MedioContacto.CORREO),
            direccionPrueba,
            documentoPrueba,
            "Roberto",
            "Carlos",
            56);

    List<Persona> ListaPersonas = new ArrayList<>();

    List<Persona> ListaImportar = new ArrayList<>();
    ListaImportar.add(personaPrueba);
    ListaImportar.add(personaPrueba2);

    ImportadorCSV importador = new ImportadorCSV(ListaPersonas);

    // el archivo tiene 19986 registros
    importador.importar("../enunciado/CSV/donantes_import_20000_UTF8_BOM.csv");

    assertEquals(19986, importador.getRegistroPersonas().size());
  }
}
