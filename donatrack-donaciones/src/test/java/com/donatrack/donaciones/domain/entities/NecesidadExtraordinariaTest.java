package com.donatrack.donaciones.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.donaciones.domain.entities.donacion.Bien;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.enums.EstadoNecesidad;
import com.donatrack.donaciones.domain.entities.necesidades.NecesidadExtraordinaria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NecesidadExtraordinariaTest {

    private Subcategoria subcategoria;
    private NecesidadExtraordinaria necesidad;

    @BeforeEach
    void setUp() {
        subcategoria = new Subcategoria("Alimentos", "Alimentos no perecederos");
        necesidad = new NecesidadExtraordinaria("Necesitamos arroz", subcategoria, 100.0);
    }

    @Test
    public void testCreacionNecesidadExtraordinaria() {
        assertEquals("Necesitamos arroz", necesidad.getDescripcion());
        assertEquals(subcategoria, necesidad.getSubcategoriaRequerida());
        assertEquals(100.0, necesidad.getCantidadRequerida());
        assertEquals(EstadoNecesidad.ABIERTA, necesidad.getEstado());
        assertTrue(necesidad.getDonacionesRecibidas().isEmpty());
        assertNotNull(necesidad.getId());
    }

    @Test
    public void testAcumularDonacionesParciales() {
        Donacion donacion = crearDonacionConBien(30.0);
        necesidad.acumularDonacionesParciales(donacion);

        assertEquals(1, necesidad.getDonacionesRecibidas().size());
        assertEquals(30.0, necesidad.cantidadAcumulada());
        assertEquals(EstadoDonacion.ASIGNADA, donacion.getEstado());
    }

    @Test
    public void testEstaCubiertaConDonacionesParciales() {
        necesidad.acumularDonacionesParciales(crearDonacionConBien(40.0));
        assertFalse(necesidad.estaCubierta());

        necesidad.acumularDonacionesParciales(crearDonacionConBien(60.0));
        assertTrue(necesidad.estaCubierta());
    }

    @Test
    public void testCambiaEstadoASatisfechaAlCubrir() {
        necesidad.acumularDonacionesParciales(crearDonacionConBien(100.0));

        assertEquals(EstadoNecesidad.SATISFECHA, necesidad.getEstado());
    }

    @Test
    public void testCantidadPendiente() {
        assertEquals(100.0, necesidad.cantidadPendiente());

        necesidad.acumularDonacionesParciales(crearDonacionConBien(30.0));
        assertEquals(70.0, necesidad.cantidadPendiente());

        necesidad.acumularDonacionesParciales(crearDonacionConBien(70.0));
        assertEquals(0.0, necesidad.cantidadPendiente());
    }

    @Test
    public void testSuperarCantidadRequerida() {
        necesidad.acumularDonacionesParciales(crearDonacionConBien(150.0));

        assertTrue(necesidad.estaCubierta());
        assertEquals(-50.0, necesidad.cantidadPendiente());
        assertEquals(EstadoNecesidad.SATISFECHA, necesidad.getEstado());
    }

    private Donacion crearDonacionConBien(double cantidad) {
        Donacion donacion = new Donacion(subcategoria);
        Bien bien = new Bien("Arroz", cantidad, "kg", false, null);
        donacion.agregarBien(bien);
        return donacion;
    }
}
