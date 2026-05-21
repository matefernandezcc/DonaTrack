package com.donatrack.donaciones.domain.model;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.donatrack.donaciones.domain.enums.MedioContacto;
import com.donatrack.donaciones.domain.enums.TipoDocumento;


public class ImportadorCSVTest {

    Pais Argentina = new Pais("Argentina", "Argentino");
    Provincia provinciaPrueba = new Provincia("Buenos Aires", Argentina);
    Coordenada coordenadaPrueba = new Coordenada(-34.6037, -58.3816);
    Direccion direccionPrueba = new Direccion("calleFalsa", 123, "Springfield", provinciaPrueba, Argentina, "1234", coordenadaPrueba);

    PersonaHumana personaPrueba = new PersonaHumana(
            "emailprueba@prueba.com",
            new Contacto("emailprueba@prueba.com", "123456789", "987654321", MedioContacto.CORREO),
            direccionPrueba,
            "juan",
            "perez",
            TipoDocumento.DNI,
            "123465879",
            30
    );
    
    @Test public void testImportar() {
        PersonaHumana personaPrueba2 = new PersonaHumana(
            "emailABuscar@prueba.com",
            new Contacto("emailABuscar@prueba.com", "123456789", "987654321", MedioContacto.CORREO),
            direccionPrueba,
            "juan",
            "perez",
            TipoDocumento.DNI,
            "123465879",
            30
        );

        List<Persona> ListaPersonas = new ArrayList<>();

        List<Persona> ListaImportar = new ArrayList<>();
        ListaImportar.add(personaPrueba);
        ListaImportar.add(personaPrueba2);

        ImportadorCSV importador = new ImportadorCSV(ListaPersonas);

        //el archivo tiene 20000 registros
        importador.importar("C:\\Users\\Admin\\Desktop\\tomi\\Carpeta con codigo\\DiseñoDeSistemas\\Donatrack\\DonaTrack\\enunciado\\CSV\\donantes_import_20000_UTF8_BOM.csv");
        
        assertEquals(19986, importador.getRegistroPersonas().size());
    }
}
