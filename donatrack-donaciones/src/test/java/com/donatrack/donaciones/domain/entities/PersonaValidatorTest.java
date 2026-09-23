package com.donatrack.donaciones.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.donaciones.domain.entities.enums.MedioContacto;
import com.donatrack.donaciones.domain.entities.enums.TipoDocumento;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.DocumentoIdentidad;
import com.donatrack.donaciones.domain.entities.persona.PersonaHumana;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.*;
import com.donatrack.donaciones.domain.entities.persona.validador.DocumentoRepetidoValidator;
import com.donatrack.donaciones.domain.entities.persona.validador.PersonaValidator;
import com.donatrack.donaciones.domain.entities.persona.validador.TelefonoRepetidoValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PersonaValidatorTest {

    private PersonaHumana persona;

    @BeforeEach
    void setUp() {
        Pais argentina = new Pais("Argentina", "Argentino");
        Provincia provincia = new Provincia("Buenos Aires", argentina);
        Coordenada coordenada = new Coordenada(-34.6037, -58.3816);
        Direccion direccion = new Direccion("Calle Falsa", 123, "Springfield", provincia, "1234", coordenada);
        DocumentoIdentidad documento = new DocumentoIdentidad(TipoDocumento.DNI, "43637832");
        Contacto contacto = new Contacto("test@test.com", "123456789", "987654321", MedioContacto.CORREO);

        persona = new PersonaHumana("test@test.com", contacto, direccion, documento, "Juan", "Pérez", 30);
    }

    @Test
    public void testDocumentoRepetidoValidatorPasaSiNoHayDuplicados() {
        // El validador actual no verifica contra repositorio (stub), siempre pasa
        DocumentoRepetidoValidator validator = new DocumentoRepetidoValidator();
        assertTrue(validator.validar(persona));
    }

    @Test
    public void testTelefonoRepetidoValidatorPasaSiNoHayDuplicados() {
        // El validador actual no verifica contra repositorio (stub), siempre pasa
        TelefonoRepetidoValidator validator = new TelefonoRepetidoValidator();
        assertTrue(validator.validar(persona));
    }

    @Test
    public void testCadenaDeValidadoresPasaTodos() {
        DocumentoRepetidoValidator docValidator = new DocumentoRepetidoValidator();
        TelefonoRepetidoValidator telValidator = new TelefonoRepetidoValidator();

        // Chain of Responsibility: doc -> tel
        docValidator.enlazarCon(telValidator);

        assertTrue(docValidator.validar(persona));
    }

    @Test
    public void testEnlazarConRetornaSiguienteValidador() {
        DocumentoRepetidoValidator docValidator = new DocumentoRepetidoValidator();
        TelefonoRepetidoValidator telValidator = new TelefonoRepetidoValidator();

        PersonaValidator resultado = docValidator.enlazarCon(telValidator);

        // enlazarCon devuelve el siguiente validador para permitir encadenamiento
        assertEquals(telValidator, resultado);
    }
}
