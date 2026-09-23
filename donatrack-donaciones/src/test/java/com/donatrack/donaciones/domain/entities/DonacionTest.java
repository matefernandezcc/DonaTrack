package com.donatrack.donaciones.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.donaciones.domain.entities.donacion.*;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

public class DonacionTest {

    private Subcategoria subcategoria;
    private Donacion donacion;

    @BeforeEach
    void setUp() {
        subcategoria = new Subcategoria("Ropa", "Ropa en general");
        donacion = new Donacion(subcategoria);
    }

    @Test
    public void testCreacionDonacionEstadoInicial() {
        assertEquals(EstadoDonacion.EN_DEPOSITO, donacion.getEstado());
        assertNotNull(donacion.getId());
        assertEquals(LocalDate.now(), donacion.getFechaCreacion());
        assertTrue(donacion.getBienes().isEmpty());
        assertTrue(donacion.getHistorial().isEmpty());
        assertTrue(donacion.getFotos().isEmpty());
        assertEquals(subcategoria, donacion.getSubCategoria());
    }

    @Test
    public void testAgregarBien() {
        Bien bien = new Bien("Campera", 2.0, "unidad", false, null);
        donacion.agregarBien(bien);

        assertEquals(1, donacion.getBienes().size());
        assertEquals(bien, donacion.getBienes().get(0));
    }

    @Test
    public void testCambiarEstadoActualizaHistorial() {
        donacion.cambiarEstado(EstadoDonacion.ASIGNADA, "Test asignación", "user-1");

        assertEquals(EstadoDonacion.ASIGNADA, donacion.getEstado());
        assertEquals(1, donacion.getHistorial().size());

        HistorialEstado historial = donacion.getHistorial().get(0);
        assertEquals(EstadoDonacion.ASIGNADA, historial.getEstado());
        assertEquals("Test asignación", historial.getObservacion());
        assertEquals("user-1", historial.getUsuarioId());
    }

    @Test
    public void testCambiarEstadoMultiplesVecesAcumulaHistorial() {
        donacion.cambiarEstado(EstadoDonacion.ASIGNADA, "Paso 1", null);
        donacion.cambiarEstado(EstadoDonacion.EN_TRASLADO, "Paso 2", null);
        donacion.cambiarEstado(EstadoDonacion.ENTREGADA, "Paso 3", null);

        assertEquals(EstadoDonacion.ENTREGADA, donacion.getEstado());
        assertEquals(3, donacion.getHistorial().size());
    }

    @Test
    public void testAsignarBeneficiario() {
        Beneficiario beneficiario = new Beneficiario();
        donacion.asignar(beneficiario);

        assertEquals(beneficiario, donacion.getEntidadAsignada());
        assertEquals(EstadoDonacion.ASIGNADA, donacion.getEstado());
        assertEquals(1, donacion.getHistorial().size());
    }

    @Test
    public void testAddFoto() {
        Foto foto = new Foto("Foto de la donación", "http://example.com/foto.jpg");
        donacion.addFoto(foto);

        assertEquals(1, donacion.getFotos().size());
        assertEquals("http://example.com/foto.jpg", donacion.getFotos().get(0).getUrl());
    }

    @Test
    public void testGetCategoriasString() {
        Subcategoria subRopa = new Subcategoria("Ropa invierno", "Ropa de invierno");
        Subcategoria subMuebles = new Subcategoria("Muebles", "Muebles varios");

        Bien bien1 = new Bien("Campera", 1.0, "unidad", false, null);
        bien1.setSubcategoria(subRopa);
        Bien bien2 = new Bien("Silla", 1.0, "unidad", true, null);
        bien2.setSubcategoria(subMuebles);

        donacion.agregarBien(bien1);
        donacion.agregarBien(bien2);

        List<String> categorias = donacion.getCategoriasString();
        assertEquals(2, categorias.size());
        assertTrue(categorias.contains("Ropa invierno"));
        assertTrue(categorias.contains("Muebles"));
    }
}
