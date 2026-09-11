package com.donatrack.donaciones.domain.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.necesidades.NecesidadExtraordinaria;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.asignador.AlgoritmoAsignacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class MatchmakerServiceTest {

    private Subcategoria subRopa;
    private Subcategoria subAlimentos;

    @BeforeEach
    void setUp() {
        subRopa = new Subcategoria("Ropa", "Ropa");
        subAlimentos = new Subcategoria("Alimentos", "Alimentos");
    }

    @Test
    public void testObtenerSugerenciasConInterseccion() {
        // Setup: ambos algoritmos sugieren el mismo beneficiario
        Beneficiario beneficiario = new Beneficiario();
        NecesidadExtraordinaria necesidad = new NecesidadExtraordinaria("Necesito ropa", subRopa, 10);
        beneficiario.registrarNecesidad(necesidad);

        Donacion donacion = new Donacion(subRopa);

        // Mock algoritmos que ambos devuelven match con la misma necesidad
        AlgoritmoAsignacion mockPrimario = mock(AlgoritmoAsignacion.class);
        AlgoritmoAsignacion mockSecundario = mock(AlgoritmoAsignacion.class);

        ResultadoMatch match = new ResultadoMatch(donacion, null, necesidad);
        when(mockPrimario.recomendarNecesidades(anyList(), anyList())).thenReturn(List.of(match));
        when(mockSecundario.recomendarNecesidades(anyList(), anyList())).thenReturn(List.of(match));

        MatchmakerService service = new MatchmakerService(mockPrimario, mockSecundario);

        List<Beneficiario> sugerencias = service.obtenerSugerencias(donacion, List.of(beneficiario));

        // Intersección: el beneficiario aparece en ambos → devuelve intersección
        assertFalse(sugerencias.isEmpty());
        assertTrue(sugerencias.contains(beneficiario));
    }

    @Test
    public void testObtenerSugerenciasSinInterseccionDevuelveUnion() {
        // Setup: cada algoritmo sugiere un beneficiario diferente
        Beneficiario beneficiario1 = new Beneficiario();
        NecesidadExtraordinaria necesidad1 = new NecesidadExtraordinaria("Necesito ropa", subRopa, 10);
        beneficiario1.registrarNecesidad(necesidad1);

        Beneficiario beneficiario2 = new Beneficiario();
        NecesidadExtraordinaria necesidad2 = new NecesidadExtraordinaria("Necesito alimentos", subAlimentos, 10);
        beneficiario2.registrarNecesidad(necesidad2);

        Donacion donacion = new Donacion(subRopa);

        AlgoritmoAsignacion mockPrimario = mock(AlgoritmoAsignacion.class);
        AlgoritmoAsignacion mockSecundario = mock(AlgoritmoAsignacion.class);

        ResultadoMatch match1 = new ResultadoMatch(donacion, null, necesidad1);
        ResultadoMatch match2 = new ResultadoMatch(donacion, null, necesidad2);

        when(mockPrimario.recomendarNecesidades(anyList(), anyList())).thenReturn(List.of(match1));
        when(mockSecundario.recomendarNecesidades(anyList(), anyList())).thenReturn(List.of(match2));

        MatchmakerService service = new MatchmakerService(mockPrimario, mockSecundario);

        List<Beneficiario> sugerencias = service.obtenerSugerencias(
                donacion, List.of(beneficiario1, beneficiario2));

        // Unión: distintos beneficiarios, no hay intersección
        assertEquals(2, sugerencias.size());
    }

    @Test
    public void testObtenerSugerenciasSinBeneficiariosDisponibles() {
        AlgoritmoAsignacion mockPrimario = mock(AlgoritmoAsignacion.class);
        AlgoritmoAsignacion mockSecundario = mock(AlgoritmoAsignacion.class);

        when(mockPrimario.recomendarNecesidades(anyList(), anyList())).thenReturn(List.of());
        when(mockSecundario.recomendarNecesidades(anyList(), anyList())).thenReturn(List.of());

        MatchmakerService service = new MatchmakerService(mockPrimario, mockSecundario);

        Donacion donacion = new Donacion(subRopa);
        List<Beneficiario> sugerencias = service.obtenerSugerencias(donacion, new ArrayList<>());

        assertTrue(sugerencias.isEmpty());
    }

    @Test
    public void testObtenerSugerenciasConConstructorPorDefecto() {
        // Verifica que el constructor sin argumentos no falla
        MatchmakerService service = new MatchmakerService();
        Donacion donacion = new Donacion(subRopa);
        List<Beneficiario> sugerencias = service.obtenerSugerencias(donacion, new ArrayList<>());

        assertNotNull(sugerencias);
    }
}
