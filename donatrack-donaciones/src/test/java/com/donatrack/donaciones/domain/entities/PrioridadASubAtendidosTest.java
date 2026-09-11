package com.donatrack.donaciones.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.necesidades.NecesidadExtraordinaria;
import com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.asignador.PrioridadASubAtendidos;
import com.donatrack.donaciones.domain.services.ResultadoMatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PrioridadASubAtendidosTest {

    private PrioridadASubAtendidos strategy;
    private Subcategoria sub;

    @BeforeEach
    void setUp() {
        strategy = new PrioridadASubAtendidos();
        sub = new Subcategoria("General", "General");
    }

    @Test
    public void testAsignaciónPorOrdenPosicional() {
        Donacion d1 = new Donacion(sub);
        Donacion d2 = new Donacion(sub);
        NecesidadExtraordinaria n1 = new NecesidadExtraordinaria("N1", sub, 10);
        NecesidadExtraordinaria n2 = new NecesidadExtraordinaria("N2", sub, 20);

        List<ResultadoMatch> matches = strategy.recomendarNecesidades(
                List.of(d1, d2), List.of(n1, n2));

        assertEquals(2, matches.size());
        assertEquals(d1, matches.get(0).getDonacion());
        assertEquals(n1, matches.get(0).getNecesidad());
        assertEquals(d2, matches.get(1).getDonacion());
        assertEquals(n2, matches.get(1).getNecesidad());
    }

    @Test
    public void testMasDonacionesQueNecesidades() {
        Donacion d1 = new Donacion(sub);
        Donacion d2 = new Donacion(sub);
        Donacion d3 = new Donacion(sub);
        NecesidadExtraordinaria n1 = new NecesidadExtraordinaria("N1", sub, 10);

        List<ResultadoMatch> matches = strategy.recomendarNecesidades(
                List.of(d1, d2, d3), List.of(n1));

        // Solo asigna min(3, 1) = 1
        assertEquals(1, matches.size());
    }

    @Test
    public void testMasNecesidadesQueDonaciones() {
        Donacion d1 = new Donacion(sub);
        NecesidadExtraordinaria n1 = new NecesidadExtraordinaria("N1", sub, 10);
        NecesidadExtraordinaria n2 = new NecesidadExtraordinaria("N2", sub, 20);
        NecesidadExtraordinaria n3 = new NecesidadExtraordinaria("N3", sub, 30);

        List<ResultadoMatch> matches = strategy.recomendarNecesidades(
                List.of(d1), List.of(n1, n2, n3));

        // Solo asigna min(1, 3) = 1
        assertEquals(1, matches.size());
    }

    @Test
    public void testListasVacias() {
        List<ResultadoMatch> matches = strategy.recomendarNecesidades(List.of(), List.of());
        assertTrue(matches.isEmpty());
    }
}
