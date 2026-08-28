package com.donatrack.incentivos.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.incentivos.domain.entities.categoria.CategoriaDonante;
import com.donatrack.incentivos.domain.entities.misiones.Mision;
import com.donatrack.incentivos.domain.entities.misiones.MisionesFactory;

import org.junit.jupiter.api.Test;

import java.util.Queue;

public class MisionesFactoryTest {

    @Test
    public void testCrearMisionesParaColaborador() {
        Queue<Mision> misiones = MisionesFactory.crearMisionesPara(CategoriaDonante.COLABORADOR);

        assertEquals(2, misiones.size());

        Mision primera = misiones.poll();
        assertNotNull(primera);
        assertEquals("Lograr 2 donaciones exitosas", primera.getNombre());

        Mision segunda = misiones.poll();
        assertNotNull(segunda);
        assertEquals("Racha 2 meses", segunda.getNombre());
    }

    @Test
    public void testCrearMisionesParaSostenedor() {
        Queue<Mision> misiones = MisionesFactory.crearMisionesPara(CategoriaDonante.SOSTENEDOR);

        assertEquals(3, misiones.size());

        Mision primera = misiones.poll();
        assertNotNull(primera);
        assertTrue(primera.getNombre().contains("5 bienes"));
    }

    @Test
    public void testCrearMisionesParaTransformador() {
        Queue<Mision> misiones = MisionesFactory.crearMisionesPara(CategoriaDonante.TRANSFORMADOR);

        assertEquals(2, misiones.size());

        Mision primera = misiones.poll();
        assertNotNull(primera);
        assertTrue(primera.getNombre().contains("10 donaciones"));
    }

    @Test
    public void testCadaMisionTieneRecompensaInsignia() {
        for (CategoriaDonante categoria : CategoriaDonante.values()) {
            Queue<Mision> misiones = MisionesFactory.crearMisionesPara(categoria);
            for (Mision mision : misiones) {
                assertNotNull(mision.getRecompensa(), 
                    "Misión '" + mision.getNombre() + "' de " + categoria + " no tiene recompensa");
                assertNotNull(mision.getRecompensa().getNombre());
            }
        }
    }
}
