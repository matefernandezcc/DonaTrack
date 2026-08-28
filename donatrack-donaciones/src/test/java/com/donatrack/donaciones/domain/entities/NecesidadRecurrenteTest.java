package com.donatrack.donaciones.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.necesidades.NecesidadRecurrente;
import com.donatrack.donaciones.domain.entities.necesidades.PeriodoNecesidad;
import com.donatrack.donaciones.domain.entities.necesidades.TipoPeriodo;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class NecesidadRecurrenteTest {

    @Test
    public void testCreacionNecesidadRecurrenteSemanal() {
        Subcategoria sub = new Subcategoria("Leche", "Leche entera");
        NecesidadRecurrente necesidad = new NecesidadRecurrente("Leche semanal", sub, 50.0, TipoPeriodo.SEMANAL);

        assertEquals("Leche semanal", necesidad.getDescripcion());
        assertEquals(50.0, necesidad.getCantidadObjetivo());
        assertTrue(necesidad.getActiva());
        assertNotNull(necesidad.getPeriodoActual());
        assertTrue(necesidad.getHistorialPeriodos().isEmpty());

        // El periodo semanal dura 7 días (día 0 a día 6)
        PeriodoNecesidad periodo = necesidad.getPeriodoActual();
        assertEquals(LocalDate.now(), periodo.getFechaInicio());
        assertEquals(LocalDate.now().plusDays(6), periodo.getFechaFin());
    }

    @Test
    public void testCreacionNecesidadRecurrenteMensual() {
        Subcategoria sub = new Subcategoria("Arroz", "Arroz blanco");
        NecesidadRecurrente necesidad = new NecesidadRecurrente("Arroz mensual", sub, 200.0, TipoPeriodo.MENSUAL);

        PeriodoNecesidad periodo = necesidad.getPeriodoActual();
        assertEquals(LocalDate.now(), periodo.getFechaInicio());
        assertEquals(LocalDate.now().plusDays(29), periodo.getFechaFin());
    }

    @Test
    public void testCerrarPeriodoYCrearSiguiente() {
        Subcategoria sub = new Subcategoria("Leche", "Leche");
        NecesidadRecurrente necesidad = new NecesidadRecurrente("Leche semanal", sub, 50.0, TipoPeriodo.SEMANAL);

        PeriodoNecesidad periodoAnterior = necesidad.getPeriodoActual();
        PeriodoNecesidad nuevoPeriodo = necesidad.cerrarPeriodoYCrearSiguiente();

        // El periodo anterior se archivó
        assertEquals(1, necesidad.getHistorialPeriodos().size());
        assertEquals(periodoAnterior, necesidad.getHistorialPeriodos().get(0));

        // El nuevo periodo comienza al día siguiente del fin del anterior
        assertNotNull(nuevoPeriodo);
        assertEquals(periodoAnterior.getFechaFin().plusDays(1), nuevoPeriodo.getFechaInicio());
        assertEquals(necesidad.getPeriodoActual(), nuevoPeriodo);
    }

    @Test
    public void testDarDeBajaImpidesCrearNuevosPeriodos() {
        Subcategoria sub = new Subcategoria("Leche", "Leche");
        NecesidadRecurrente necesidad = new NecesidadRecurrente("Leche semanal", sub, 50.0, TipoPeriodo.SEMANAL);

        necesidad.darDeBaja();
        assertFalse(necesidad.getActiva());

        PeriodoNecesidad resultado = necesidad.cerrarPeriodoYCrearSiguiente();
        assertNull(resultado);
        assertTrue(necesidad.getHistorialPeriodos().isEmpty());
    }
}
