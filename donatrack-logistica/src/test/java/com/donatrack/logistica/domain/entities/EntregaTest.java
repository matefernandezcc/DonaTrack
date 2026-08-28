package com.donatrack.logistica.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.logistica.domain.entities.entregas.ComprobanteRecepcion;
import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.domain.entities.entregas.EstadoEntrega;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

/**
 * Tests unitarios de la entidad Entrega del servicio de logística.
 *
 * Verifica transiciones de estado: PENDIENTE → EN_TRASLADO → ENTREGADA / NO_RECIBIDA
 * y la creación del comprobante de recepción.
 */
public class EntregaTest {

    private Entrega entrega;

    @BeforeEach
    void setUp() {
        entrega = new Entrega();
        entrega.setIdEntrega(UUID.randomUUID());
        entrega.setEstado(EstadoEntrega.PENDIENTE);
        entrega.setPesoEstimado(50.0);
        entrega.setVolumenEstimado(2.0);
    }

    @Test
    public void testMarcarEnTraslado() {
        entrega.marcarEnTraslado();
        assertEquals(EstadoEntrega.EN_TRASLADO, entrega.getEstado());
    }

    @Test
    public void testConfirmarRecepcionConFotosYPatente() {
        List<String> fotos = List.of("foto1.jpg", "foto2.jpg");
        String patente = "AB-123-CD";

        entrega.confirmarRecepcion(fotos, patente);

        assertEquals(EstadoEntrega.ENTREGADA, entrega.getEstado());
        assertNotNull(entrega.getComprobanteRecepcion());

        ComprobanteRecepcion comprobante = entrega.getComprobanteRecepcion();
        assertNotNull(comprobante.getFechaHora());
        assertEquals(2, comprobante.getFotos().size());
        assertEquals("AB-123-CD", comprobante.getCamionPatente());
    }

    @Test
    public void testConfirmarRecepcionSinPatente() {
        List<String> fotos = List.of("foto1.jpg");

        entrega.confirmarRecepcion(fotos);

        assertEquals(EstadoEntrega.ENTREGADA, entrega.getEstado());
        assertNotNull(entrega.getComprobanteRecepcion());
        assertNull(entrega.getComprobanteRecepcion().getCamionPatente());
    }

    @Test
    public void testMarcarNoRecibida() {
        entrega.marcarNoRecibida();
        assertEquals(EstadoEntrega.NO_RECIBIDA, entrega.getEstado());
    }

    @Test
    public void testVolverAPendiente() {
        // Simular que fue no recibida y después vuelve a pendiente
        entrega.marcarNoRecibida();
        entrega.volverAPendiente();
        assertEquals(EstadoEntrega.PENDIENTE, entrega.getEstado());
    }

    @Test
    public void testCicloCompletoDeEstados() {
        // PENDIENTE → EN_TRASLADO → ENTREGADA
        assertEquals(EstadoEntrega.PENDIENTE, entrega.getEstado());

        entrega.marcarEnTraslado();
        assertEquals(EstadoEntrega.EN_TRASLADO, entrega.getEstado());

        entrega.confirmarRecepcion(List.of("foto.jpg"), "AA-111-BB");
        assertEquals(EstadoEntrega.ENTREGADA, entrega.getEstado());
    }
}
