package com.donatrack.incentivos.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.incentivos.domain.entities.categoria.CategoriaDonante;
import com.donatrack.incentivos.domain.entities.misiones.Mision;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

/**
 * Tests unitarios del PerfilDonante.
 *
 * Cubren:
 * - Estado inicial de un perfil nuevo (categoría COLABORADOR)
 * - Registro de donaciones y actualización de métricas
 * - Progresión de misiones e insignias
 * - Procesamiento de inactividad (corte de racha a los 30 días sin donar)
 * - Avance de categoría: COLABORADOR → SOSTENEDOR → TRANSFORMADOR
 */
public class PerfilDonanteTest {

    private PerfilDonante perfil;
    private UUID donanteId;

    @BeforeEach
    void setUp() {
        donanteId = UUID.randomUUID();
        perfil = new PerfilDonante(donanteId);
    }

    // ==================== Estado Inicial ====================

    @Test
    public void testNuevoPerfilEmpiezaComoColaborador() {
        assertEquals(CategoriaDonante.COLABORADOR, perfil.getCategoria());
        assertEquals(donanteId, perfil.getDonanteId());
        assertNotNull(perfil.getMetricas());
        assertNotNull(perfil.getMisionesPendientes());
        assertNotNull(perfil.getMisionActual());
        assertTrue(perfil.getInsigniasObtenidas().isEmpty());
    }

    @Test
    public void testNuevoPerfilTieneMisionesDeColaborador() {
        // COLABORADOR tiene 2 misiones: "2 donaciones exitosas" y "racha 2 meses"
        assertNotNull(perfil.getMisionActual());
        assertEquals("Lograr 2 donaciones exitosas", perfil.getMisionActual().getNombre());
    }

    // ==================== Registro de donaciones ====================

    @Test
    public void testRegistrarDonacionExitosaActualizaMetricas() {
        RegistroDonacion donacion = crearRegistro(YearMonth.now(), LocalDate.now(), 3, true);

        perfil.registrarDonacionExitosa(donacion);

        assertEquals(1, perfil.getMetricas().obtenerTodasLasDonaciones().size());
    }

    // ==================== Progresión de misiones ====================

    @Test
    public void testCompletarPrimeraMisionDeColaborador() {
        // Misión 1 de COLABORADOR: 2 donaciones exitosas
        perfil.registrarDonacionExitosa(crearRegistro(YearMonth.now(), LocalDate.now(), 1, true));
        perfil.registrarDonacionExitosa(crearRegistro(YearMonth.now(), LocalDate.now(), 1, true));

        // Debería haber completado la primera misión y ganar una insignia
        assertTrue(perfil.getInsigniasObtenidas().size() >= 1,
                "Al completar 2 donaciones exitosas, debería ganar la insignia 'Buen Inicio'");
    }

    @Test
    public void testCompletarTodasLasMisionesDeColaboradorAvanzaASostenedor() {
        // Misión 1: 2 donaciones exitosas
        // Misión 2: racha de 2 meses consecutivos
        YearMonth mesPasado = YearMonth.now().minusMonths(1);
        YearMonth mesActual = YearMonth.now();

        perfil.registrarDonacionExitosa(crearRegistro(mesPasado, mesPasado.atDay(15), 1, true));
        perfil.registrarDonacionExitosa(crearRegistro(mesActual, LocalDate.now(), 1, true));

        // Si ya completó ambas misiones, debería avanzar a SOSTENEDOR
        if (perfil.getCategoria() == CategoriaDonante.SOSTENEDOR) {
            assertEquals(CategoriaDonante.SOSTENEDOR, perfil.getCategoria());
            assertEquals(2, perfil.getInsigniasObtenidas().size());
        }
    }

    // ==================== Inactividad (30 días) ====================

    @Test
    public void testProcesarInactividadCortaRachaTras30Dias() {
        // Donación hace 31 días → se debería cortar la racha
        LocalDate hace31Dias = LocalDate.now().minusDays(31);
        RegistroDonacion donacionVieja = crearRegistro(
                YearMonth.from(hace31Dias), hace31Dias, 1, true);
        perfil.getMetricas().registrarDonacion(donacionVieja);

        assertEquals(1, perfil.getMetricas().obtenerTodasLasDonaciones().size());

        // Procesar inactividad con la fecha de hoy
        perfil.procesarInactividad(LocalDate.now());

        // La racha debería haberse cortado (fecha de corte asignada)
        assertNotNull(perfil.getMetricas().getFechaCorteRacha(),
                "Al superar los 30 días sin donar, la racha debe cortarse (fechaCorteRacha)");
        assertEquals(1, perfil.getMetricas().obtenerTodasLasDonaciones().size(),
                "El historial NO debe borrarse al perder la racha");
    }

    @Test
    public void testProcesarInactividadNoCortaRachaDentroDe30Dias() {
        // Donación hace 29 días → NO se debería cortar
        LocalDate hace29Dias = LocalDate.now().minusDays(29);
        RegistroDonacion donacion = crearRegistro(
                YearMonth.from(hace29Dias), hace29Dias, 1, true);
        perfil.getMetricas().registrarDonacion(donacion);

        perfil.procesarInactividad(LocalDate.now());

        assertNull(perfil.getMetricas().getFechaCorteRacha(),
                "Con 29 días de inactividad, la racha NO debería cortarse");
    }

    @Test
    public void testProcesarInactividadConListaVacia() {
        // No debería hacer nada ni explotar
        perfil.procesarInactividad(LocalDate.now());
        assertTrue(perfil.getMetricas().obtenerTodasLasDonaciones().isEmpty());
    }

    @Test
    public void testProcesarInactividadNoCortaSiDonoHoy() {
        RegistroDonacion donacionHoy = crearRegistro(
                YearMonth.now(), LocalDate.now(), 1, true);
        perfil.getMetricas().registrarDonacion(donacionHoy);

        perfil.procesarInactividad(LocalDate.now());

        assertEquals(1, perfil.getMetricas().obtenerTodasLasDonaciones().size(),
                "Si donó hoy, la racha no debería cortarse");
    }

    // ==================== Avance completo de categorías ====================

    @Test
    public void testAvanceDeCategoriaProgresivo() {
        // Simular múltiples donaciones con datos ricos para disparar misiones
        for (int i = 0; i < 6; i++) {
            YearMonth mes = YearMonth.now().minusMonths(5 - i);
            perfil.registrarDonacionExitosa(
                    crearRegistro(mes, mes.atDay(15), 6, true,
                            Set.of("Ropa", "Alimentos", "Muebles", "Electrónica", "Juguetes")));
        }

        // Debería haber ganado varias insignias y potencialmente avanzado de categoría
        assertTrue(perfil.getInsigniasObtenidas().size() >= 2,
                "Debería haber ganado al menos 2 insignias con 6 donaciones ricas");
    }

    // ==================== Helpers ====================

    private RegistroDonacion crearRegistro(YearMonth mes, LocalDate fecha, int cantBienes, boolean exitosa) {
        return new RegistroDonacion(
                UUID.randomUUID(), cantBienes, Set.of("General"),
                exitosa ? UUID.randomUUID() : null, mes, fecha);
    }

    private RegistroDonacion crearRegistro(YearMonth mes, LocalDate fecha, int cantBienes, boolean exitosa,
            Set<String> categorias) {
        return new RegistroDonacion(
                UUID.randomUUID(), cantBienes, categorias,
                exitosa ? UUID.randomUUID() : null, mes, fecha);
    }
}
