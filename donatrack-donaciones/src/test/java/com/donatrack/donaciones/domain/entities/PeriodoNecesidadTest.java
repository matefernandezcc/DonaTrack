package com.donatrack.donaciones.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.donaciones.domain.entities.donacion.Bien;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.enums.EstadoNecesidad;
import com.donatrack.donaciones.domain.entities.necesidades.PeriodoNecesidad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class PeriodoNecesidadTest {

    private PeriodoNecesidad periodo;
    private Subcategoria subcategoria;

    @BeforeEach
    void setUp() {
        periodo = new PeriodoNecesidad(LocalDate.now(), LocalDate.now().plusDays(6));
        subcategoria = new Subcategoria("Alimentos", "Alimentos en general");
    }

    @Test
    public void testCreacionPeriodo() {
        assertEquals(LocalDate.now(), periodo.getFechaInicio());
        assertEquals(LocalDate.now().plusDays(6), periodo.getFechaFin());
        assertEquals(EstadoNecesidad.ABIERTA, periodo.getEstado());
        assertTrue(periodo.getDonacionesAsignadas().isEmpty());
        assertNotNull(periodo.getId());
    }

    @Test
    public void testCantidadAcumuladaSinDonaciones() {
        assertEquals(0.0, periodo.cantidadAcumulada());
    }

    @Test
    public void testCantidadAcumuladaConDonaciones() {
        Donacion d1 = crearDonacionConBien(20.0);
        Donacion d2 = crearDonacionConBien(30.0);

        periodo.asignarDonacion(d1, 100.0);
        periodo.asignarDonacion(d2, 100.0);

        assertEquals(50.0, periodo.cantidadAcumulada());
    }

    @Test
    public void testAsignarDonacionCambiaEstadoDonacion() {
        Donacion donacion = crearDonacionConBien(10.0);
        periodo.asignarDonacion(donacion, 100.0);

        assertEquals(EstadoDonacion.ASIGNADA, donacion.getEstado());
    }

    @Test
    public void testEstaCubierta() {
        assertFalse(periodo.estaCubierta(100.0));

        periodo.asignarDonacion(crearDonacionConBien(100.0), 100.0);

        assertTrue(periodo.estaCubierta(100.0));
        assertEquals(EstadoNecesidad.SATISFECHA, periodo.getEstado());
    }

    private Donacion crearDonacionConBien(double cantidad) {
        Donacion donacion = new Donacion(subcategoria);
        Bien bien = new Bien("Item", cantidad, "kg", false, null);
        donacion.agregarBien(bien);
        return donacion;
    }
}
