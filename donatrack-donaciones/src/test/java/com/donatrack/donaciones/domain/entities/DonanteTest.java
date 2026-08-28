package com.donatrack.donaciones.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.roles.Donante;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DonanteTest {

    private Donante donante;

    @BeforeEach
    void setUp() {
        donante = new Donante();
    }

    @Test
    public void testDonanteInicializaConListaVacia() {
        assertNotNull(donante.getDonacionesRealizadas());
        assertTrue(donante.getDonacionesRealizadas().isEmpty());
        assertNotNull(donante.getId());
        assertNotNull(donante.getFechaAlta());
    }

    @Test
    public void testAgregarDonacion() {
        DonacionOriginal donacionOriginal = new DonacionOriginal("Ropa usada", donante, "user-1");
        donante.agregarDonacion(donacionOriginal);

        assertEquals(1, donante.getDonacionesRealizadas().size());
        assertEquals(donacionOriginal, donante.getDonacionesRealizadas().get(0));
    }

    @Test
    public void testConsultarEstadoDonacion() {
        Subcategoria sub = new Subcategoria("Ropa", "Ropa general");
        Donacion donacion = new Donacion(sub);

        assertEquals(EstadoDonacion.EN_DEPOSITO, donante.consultarEstadoDonacion(donacion));

        donacion.cambiarEstado(EstadoDonacion.ENTREGADA, "Entregada", null);
        assertEquals(EstadoDonacion.ENTREGADA, donante.consultarEstadoDonacion(donacion));
    }

    @Test
    public void testEsValidoParaHumanaYJuridica() {
        assertTrue(donante.esValidoParaHumana());
        assertTrue(donante.esValidoParaJuridica());
    }
}
