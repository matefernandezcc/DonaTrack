package com.donatrack.notificaciones.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Tests unitarios del NotificacionFactory.
 *
 * Verifica que la factory crea notificaciones con los parámetros correctos,
 * tanto con medio de envío como sin él.
 */
public class NotificacionFactoryTest {

    @Test
    public void testCrearNotificacionSinMedio() {
        Evento evento = new Evento(TipoEvento.MISION_CUMPLIDA, "Misión completada",
                List.of("DONANTE"));

        Notificacion notif = NotificacionFactory.crearNotificacion(
                evento, "donante@test.com", "¡Felicidades!");

        assertEquals("donante@test.com", notif.getDestinatario());
        assertEquals("¡Felicidades!", notif.getMensaje());
        assertEquals(evento, notif.getEvento());
        assertNull(notif.getMedio());
    }

    @Test
    public void testCrearNotificacionConMedio() {
        Evento evento = new Evento(TipoEvento.CAMBIO_CATEGORIA, "Subiste de categoría",
                List.of("DONANTE"));

        Notificacion notif = NotificacionFactory.crearNotificacion(
                evento, "donante@test.com", "Subiste a SOSTENEDOR", "WHATSAPP");

        assertEquals("donante@test.com", notif.getDestinatario());
        assertEquals("Subiste a SOSTENEDOR", notif.getMensaje());
        assertEquals(evento, notif.getEvento());
        assertEquals("WHATSAPP", notif.getMedio());
    }

    @Test
    public void testNotificacionCreadaNoEstaCompletada() {
        Evento evento = new Evento(TipoEvento.INICIO_RUTA_LOGISTICA, "Ruta iniciada",
                List.of("DONANTE", "BENEFICIARIO"));

        Notificacion notif = NotificacionFactory.crearNotificacion(
                evento, "user@test.com", "Tu donación está en camino");

        assertFalse(notif.isCompletada());
        assertNotNull(notif.getFechaEnvio());
    }
}
