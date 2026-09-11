package com.donatrack.notificaciones.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Tests unitarios de la entidad Notificacion del servicio de notificaciones.
 *
 * Verifica constructores, estado inicial y marcación de completada.
 */
public class NotificacionTest {

    @Test
    public void testConstructorSimple() {
        Notificacion notif = new Notificacion("user@test.com", "Tu donación fue entregada");

        assertEquals("user@test.com", notif.getDestinatario());
        assertEquals("Tu donación fue entregada", notif.getMensaje());
        assertNotNull(notif.getFechaEnvio());
        assertFalse(notif.isCompletada());
        assertNull(notif.getEvento());
        assertNull(notif.getMedio());
    }

    @Test
    public void testConstructorConEvento() {
        Evento evento = new Evento(TipoEvento.ENTREGA_EXITOSA, "Entrega OK", List.of("DONANTE", "BENEFICIARIO"));
        Notificacion notif = new Notificacion("user@test.com", "Mensaje", evento, "EMAIL");

        assertEquals("user@test.com", notif.getDestinatario());
        assertEquals("Mensaje", notif.getMensaje());
        assertEquals(evento, notif.getEvento());
        assertEquals("EMAIL", notif.getMedio());
        assertFalse(notif.isCompletada());
    }

    @Test
    public void testMarcarComoCompletada() {
        Notificacion notif = new Notificacion("user@test.com", "Test");

        assertFalse(notif.isCompletada());

        notif.marcarComoCompletada();

        assertTrue(notif.isCompletada());
    }
}
