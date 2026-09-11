package com.donatrack.incentivos.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.incentivos.domain.entities.categoria.CategoriaDonante;
import com.donatrack.incentivos.domain.entities.misiones.Mision;
import com.donatrack.incentivos.domain.entities.misiones.TipoMetricaMision;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

public class MisionTest {

    @Test
    public void testEvaluarDonacionesExitosas() {
        Mision mision = new Mision("2 donaciones", new Insignia("Test", "Test"),
                TipoMetricaMision.DONACIONES_EXITOSAS, 2);

        PerfilDonante perfil = new PerfilDonante(UUID.randomUUID());

        assertFalse(mision.evaluar(perfil), "Sin donaciones no debería cumplirse");

        perfil.getMetricas().registrarDonacion(crearDonacionExitosa(YearMonth.now()));
        assertFalse(mision.evaluar(perfil), "Con 1 donación no debería cumplirse");

        perfil.getMetricas().registrarDonacion(crearDonacionExitosa(YearMonth.now()));
        assertTrue(mision.evaluar(perfil), "Con 2 donaciones debería cumplirse");
    }

    @Test
    public void testEvaluarMaxBienes() {
        Mision mision = new Mision("Más de 5 bienes", new Insignia("Test", "Test"),
                TipoMetricaMision.MAX_BIENES, 5);

        PerfilDonante perfil = new PerfilDonante(UUID.randomUUID());

        perfil.getMetricas().registrarDonacion(
                new RegistroDonacion(UUID.randomUUID(), 3, Set.of("Ropa"), UUID.randomUUID(), YearMonth.now()));
        assertFalse(mision.evaluar(perfil), "Con 3 bienes no debería cumplirse");

        perfil.getMetricas().registrarDonacion(
                new RegistroDonacion(UUID.randomUUID(), 6, Set.of("Ropa"), UUID.randomUUID(), YearMonth.now()));
        assertTrue(mision.evaluar(perfil), "Con 6 bienes debería cumplirse");
    }

    @Test
    public void testEvaluarMesesConsecutivos() {
        Mision mision = new Mision("Racha 2 meses", new Insignia("Test", "Test"),
                TipoMetricaMision.MESES_CONSECUTIVOS, 2);

        PerfilDonante perfil = new PerfilDonante(UUID.randomUUID());

        // Donar este mes y el mes pasado = racha de 2
        YearMonth mesActual = YearMonth.now();
        YearMonth mesPasado = mesActual.minusMonths(1);

        perfil.getMetricas().registrarDonacion(crearDonacionExitosa(mesPasado));
        perfil.getMetricas().registrarDonacion(crearDonacionExitosa(mesActual));

        assertTrue(mision.evaluar(perfil), "Con donaciones 2 meses seguidos debería cumplirse");
    }

    @Test
    public void testEvaluarMesesConsecutivosRachaRota() {
        Mision mision = new Mision("Racha 3 meses", new Insignia("Test", "Test"),
                TipoMetricaMision.MESES_CONSECUTIVOS, 3);

        PerfilDonante perfil = new PerfilDonante(UUID.randomUUID());

        YearMonth mesActual = YearMonth.now();
        // Donó este mes y hace 2 meses (falta el mes del medio)
        perfil.getMetricas().registrarDonacion(crearDonacionExitosa(mesActual));
        perfil.getMetricas().registrarDonacion(crearDonacionExitosa(mesActual.minusMonths(2)));

        assertFalse(mision.evaluar(perfil), "Racha rota no debería cumplirse");
    }

    @Test
    public void testEvaluarCategoriasDistintas() {
        Mision mision = new Mision("3 categorías distintas", new Insignia("Test", "Test"),
                TipoMetricaMision.CATEGORIAS_DISTINTAS, 3);

        PerfilDonante perfil = new PerfilDonante(UUID.randomUUID());

        perfil.getMetricas().registrarDonacion(
                new RegistroDonacion(UUID.randomUUID(), 1, Set.of("Ropa"), UUID.randomUUID(), YearMonth.now()));
        assertFalse(mision.evaluar(perfil));

        perfil.getMetricas().registrarDonacion(
                new RegistroDonacion(UUID.randomUUID(), 1, Set.of("Alimentos", "Muebles"), UUID.randomUUID(),
                        YearMonth.now()));
        assertTrue(mision.evaluar(perfil), "Con 3 categorías distintas debería cumplirse");
    }

    @Test
    public void testGetProgresoActual() {
        Mision mision = new Mision("5 donaciones", new Insignia("Test", "Test"),
                TipoMetricaMision.DONACIONES_EXITOSAS, 5);

        PerfilDonante perfil = new PerfilDonante(UUID.randomUUID());
        perfil.getMetricas().registrarDonacion(crearDonacionExitosa(YearMonth.now()));
        perfil.getMetricas().registrarDonacion(crearDonacionExitosa(YearMonth.now()));

        assertEquals("2 / 5", mision.getProgresoActual(perfil));
    }

    @Test
    public void testEvaluarConMetricasNull() {
        Mision mision = new Mision("Test", new Insignia("Test", "Test"),
                TipoMetricaMision.DONACIONES_EXITOSAS, 1);

        PerfilDonante perfil = new PerfilDonante(UUID.randomUUID());
        perfil.setMetricas(null);

        assertFalse(mision.evaluar(perfil));
    }

    // ========================= Helper =========================

    private RegistroDonacion crearDonacionExitosa(YearMonth mes) {
        return new RegistroDonacion(UUID.randomUUID(), 1, Set.of("General"), UUID.randomUUID(), mes);
    }
}
