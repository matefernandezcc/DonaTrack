package com.donatrack.donaciones.domain.entities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.donacion.Archivo;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.persona.PersonaHumana;
import com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.importador.ImportadorCSV;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Tests unitarios autocontenidos para ImportadorCSV.
 *
 * Valida la importación de donantes (PersonaHumana y PersonaJuridica) desde CSV
 * sin depender de archivos externos del sistema de archivos.
 */
public class ImportadorCSVTest {

    private PersonaRepository mockRepository;
    private ImportadorCSV importador;

    @BeforeEach
    void setUp() {
        mockRepository = mock(PersonaRepository.class);
        importador = new ImportadorCSV(mockRepository);
    }

    @Test
    public void testImportarNuevasPersonasDesdeCSV() {
        String csvContent = "TipoPersona,TipoDoc,Documento,Nombre/Razón Social,Email,Teléfono\n"
                + "HUMANA,DNI,28456905,Ana Navarro,ananavarro@test.com,+54 11 5181-9600\n"
                + "JURIDICA,CUIT,30711223344,Fundación Esperanza,contacto@fundacion.org,+54 11 4321-8888\n";

        Archivo archivo = new Archivo("donantes.csv", csvContent.getBytes(StandardCharsets.UTF_8));

        when(mockRepository.buscarPorEmail(anyString())).thenReturn(Optional.empty());

        importador.importar(archivo);

        // Se deben haber guardado 2 personas nuevas en el repositorio
        verify(mockRepository, times(2)).guardar(any(Persona.class));
        verify(mockRepository).buscarPorEmail("ananavarro@test.com");
        verify(mockRepository).buscarPorEmail("contacto@fundacion.org");
    }

    @Test
    public void testImportarActualizaPersonaExistente() {
        String csvContent = "TipoPersona,TipoDoc,Documento,Nombre/Razón Social,Email,Teléfono\n"
                + "HUMANA,DNI,28456905,Ana Navarro Actualizada,ananavarro@test.com,+54 11 9999-0000\n";

        Archivo archivo = new Archivo("donantes.csv", csvContent.getBytes(StandardCharsets.UTF_8));

        PersonaHumana existente = new PersonaHumana("ananavarro@test.com", null, null, null, "Ana", "Navarro", 30);
        when(mockRepository.buscarPorEmail("ananavarro@test.com")).thenReturn(Optional.of(existente));

        importador.importar(archivo);

        // Debe buscar por email y guardar la persona existente actualizada
        verify(mockRepository).buscarPorEmail("ananavarro@test.com");
        verify(mockRepository).guardar(existente);
    }

    @Test
    public void testImportarArchivoNullOSinContenidoNoHaceNada() {
        assertDoesNotThrow(() -> importador.importar(null));
        assertDoesNotThrow(() -> importador.importar(new Archivo("vacio.csv", null)));
        assertDoesNotThrow(() -> importador.importar(new Archivo("vacio.csv", new byte[0])));

        verify(mockRepository, never()).guardar(any());
    }

    @Test
    public void testGetRegistroPersonasDelegaAlRepositorio() {
        when(mockRepository.obtenerTodas()).thenReturn(List.of());

        List<Persona> resultado = importador.getRegistroPersonas();

        assertNotNull(resultado);
        verify(mockRepository).obtenerTodas();
    }
}
