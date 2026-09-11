package com.donatrack.logistica.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.domain.entities.entregas.EstadoEntrega;
import com.donatrack.logistica.domain.entities.reparto.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tests unitarios de la entidad RutaDeReparto.
 *
 * Verifica que iniciarRuta() marca la ruta como iniciada y cambia el
 * estado de todas las entregas en todas las paradas a EN_TRASLADO.
 */
public class RutaDeRepartoTest {

    @Test
    public void testIniciarRutaMarcaIniciada() {
        RutaDeReparto ruta = crearRutaConEntregas(2, 2);

        ruta.iniciarRuta();

        assertTrue(ruta.getIniciada());
    }

    @Test
    public void testIniciarRutaCambiaEstadoDeTodasLasEntregas() {
        RutaDeReparto ruta = crearRutaConEntregas(2, 3);

        ruta.iniciarRuta();

        // Todas las entregas de todas las paradas deberían estar EN_TRASLADO
        for (Parada parada : ruta.getParadas()) {
            for (Entrega entrega : parada.getEntregas()) {
                assertEquals(EstadoEntrega.EN_TRASLADO, entrega.getEstado(),
                        "Todas las entregas deben pasar a EN_TRASLADO al iniciar la ruta");
            }
        }
    }

    @Test
    public void testIniciarRutaSinParadasNoFalla() {
        RutaDeReparto ruta = new RutaDeReparto();
        ruta.setId(UUID.randomUUID());
        ruta.setFechaOperativa(LocalDate.now());
        ruta.setIniciada(false);
        ruta.setParadas(null);

        // No debería tirar NullPointerException
        assertDoesNotThrow(() -> ruta.iniciarRuta());
        assertTrue(ruta.getIniciada());
    }

    @Test
    public void testIniciarRutaConParadaSinEntregasNoFalla() {
        RutaDeReparto ruta = new RutaDeReparto();
        ruta.setId(UUID.randomUUID());
        ruta.setFechaOperativa(LocalDate.now());
        ruta.setIniciada(false);

        Parada paradaVacia = new Parada();
        paradaVacia.setOrden(1);
        paradaVacia.setEntregas(null);

        ruta.setParadas(List.of(paradaVacia));

        assertDoesNotThrow(() -> ruta.iniciarRuta());
        assertTrue(ruta.getIniciada());
    }

    @Test
    public void testRutaConCamionYChofer() {
        Camion camion = new Camion("AB-123-CD", 15.0, 3.0, 5000.0);
        Chofer chofer = new Chofer();

        RutaDeReparto ruta = crearRutaConEntregas(1, 1);
        ruta.setCamion(camion);
        ruta.setChofer(chofer);

        assertEquals("AB-123-CD", ruta.getCamion().getPatente());
        assertEquals(15.0, ruta.getCamion().getCapacidadVolumen());
        assertEquals(3.0, ruta.getCamion().getAltura());
        assertEquals(5000.0, ruta.getCamion().getCapacidadCarga());
    }

    // ========================= Helper =========================

    private RutaDeReparto crearRutaConEntregas(int cantParadas, int entregasPorParada) {
        RutaDeReparto ruta = new RutaDeReparto();
        ruta.setId(UUID.randomUUID());
        ruta.setFechaOperativa(LocalDate.now());
        ruta.setIniciada(false);

        List<Parada> paradas = new ArrayList<>();
        for (int i = 0; i < cantParadas; i++) {
            Parada parada = new Parada();
            parada.setOrden(i + 1);
            parada.setDireccion(new Direccion());

            List<Entrega> entregas = new ArrayList<>();
            for (int j = 0; j < entregasPorParada; j++) {
                Entrega entrega = new Entrega();
                entrega.setIdEntrega(UUID.randomUUID());
                entrega.setEstado(EstadoEntrega.PENDIENTE);
                entregas.add(entrega);
            }
            parada.setEntregas(entregas);
            paradas.add(parada);
        }
        ruta.setParadas(paradas);

        return ruta;
    }
}
