package com.donatrack.donaciones.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.necesidades.NecesidadExtraordinaria;
import com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.asignador.CompatibilidadSemantica;
import com.donatrack.donaciones.domain.services.ResultadoMatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CompatibilidadSemanticaTest {

    private CompatibilidadSemantica strategy;
    private Subcategoria subRopa;
    private Subcategoria subAlimentos;

    @BeforeEach
    void setUp() {
        strategy = new CompatibilidadSemantica();
        subRopa = new Subcategoria("Ropa", "Ropa en general");
        subAlimentos = new Subcategoria("Alimentos", "Alimentos en general");
    }

    @Test
    public void testDonacionMatcheaNecesidadMismaSubcategoria() {
        Donacion donacion = new Donacion(subRopa);
        NecesidadExtraordinaria necesidad = new NecesidadExtraordinaria("Necesitamos ropa", subRopa, 10);

        List<ResultadoMatch> matches = strategy.recomendarNecesidades(List.of(donacion), List.of(necesidad));

        assertEquals(1, matches.size());
        assertEquals(donacion, matches.get(0).getDonacion());
        assertEquals(necesidad, matches.get(0).getNecesidad());
    }

    @Test
    public void testDonacionNoMatcheaNecesidadDistintaSubcategoria() {
        Donacion donacion = new Donacion(subRopa);
        NecesidadExtraordinaria necesidad = new NecesidadExtraordinaria("Necesitamos alimentos", subAlimentos, 10);

        List<ResultadoMatch> matches = strategy.recomendarNecesidades(List.of(donacion), List.of(necesidad));

        assertTrue(matches.isEmpty());
    }

    @Test
    public void testDonacionSinSubcategoriaNoMatchea() {
        Donacion donacion = new Donacion(null);
        NecesidadExtraordinaria necesidad = new NecesidadExtraordinaria("Necesitamos ropa", subRopa, 10);

        List<ResultadoMatch> matches = strategy.recomendarNecesidades(List.of(donacion), List.of(necesidad));

        assertTrue(matches.isEmpty());
    }

    @Test
    public void testDonacionMatcheaSoloPrimeraNecesidadCompatible() {
        Donacion donacion = new Donacion(subRopa);
        NecesidadExtraordinaria necesidad1 = new NecesidadExtraordinaria("Ropa 1", subRopa, 10);
        NecesidadExtraordinaria necesidad2 = new NecesidadExtraordinaria("Ropa 2", subRopa, 20);

        List<ResultadoMatch> matches = strategy.recomendarNecesidades(
                List.of(donacion), List.of(necesidad1, necesidad2));

        // Cada donación matchea máximo 1 necesidad (por el break)
        assertEquals(1, matches.size());
        assertEquals(necesidad1, matches.get(0).getNecesidad());
    }

    @Test
    public void testMultiplesDonacionesYNecesidades() {
        Donacion donacionRopa = new Donacion(subRopa);
        Donacion donacionAlimentos = new Donacion(subAlimentos);
        NecesidadExtraordinaria necesidadRopa = new NecesidadExtraordinaria("Ropa", subRopa, 10);
        NecesidadExtraordinaria necesidadAlimentos = new NecesidadExtraordinaria("Alimentos", subAlimentos, 20);

        List<ResultadoMatch> matches = strategy.recomendarNecesidades(
                List.of(donacionRopa, donacionAlimentos),
                List.of(necesidadRopa, necesidadAlimentos));

        assertEquals(2, matches.size());
    }
}
