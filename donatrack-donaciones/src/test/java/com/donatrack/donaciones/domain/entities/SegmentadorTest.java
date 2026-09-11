package com.donatrack.donaciones.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.donaciones.domain.entities.donacion.Bien;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.donacion.segmentador.SegmentarPorEstado;
import com.donatrack.donaciones.domain.entities.donacion.segmentador.SegmentarPorSubcategoria;
import com.donatrack.donaciones.domain.entities.donacion.segmentador.SegmentarPorVencimiento;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SegmentadorTest {

    // ========================= SegmentarPorSubcategoria =========================

    @Test
    public void testSegmentarPorSubcategoriaAgrupaCorrectamente() {
        Subcategoria subRopa = new Subcategoria("Ropa", "Ropa");
        Subcategoria subMuebles = new Subcategoria("Muebles", "Muebles");

        Bien bien1 = crearBien("Campera", subRopa, null, null);
        Bien bien2 = crearBien("Bufanda", subRopa, null, null);
        Bien bien3 = crearBien("Silla", subMuebles, null, null);

        List<List<Bien>> input = new ArrayList<>();
        input.add(List.of(bien1, bien2, bien3));

        SegmentarPorSubcategoria estrategia = new SegmentarPorSubcategoria();
        List<List<Bien>> resultado = estrategia.segmentar(input);

        // Debe separar en 2 grupos: Ropa (2 bienes) y Muebles (1 bien)
        assertEquals(2, resultado.size());
    }

    @Test
    public void testSegmentarPorSubcategoriaMismaSubcategoria() {
        Subcategoria sub = new Subcategoria("Ropa", "Ropa");

        Bien bien1 = crearBien("Campera", sub, null, null);
        Bien bien2 = crearBien("Pantalón", sub, null, null);

        List<List<Bien>> input = new ArrayList<>();
        input.add(List.of(bien1, bien2));

        SegmentarPorSubcategoria estrategia = new SegmentarPorSubcategoria();
        List<List<Bien>> resultado = estrategia.segmentar(input);

        assertEquals(1, resultado.size());
        assertEquals(2, resultado.get(0).size());
    }

    // ========================= SegmentarPorVencimiento =========================

    @Test
    public void testSegmentarPorVencimientoSinFechaVencimientoPasaIntacto() {
        Subcategoria sub = new Subcategoria("Ropa", "Ropa");

        Bien bien1 = crearBien("Campera", sub, null, null);
        Bien bien2 = crearBien("Pantalón", sub, null, null);

        List<List<Bien>> input = new ArrayList<>();
        input.add(List.of(bien1, bien2));

        SegmentarPorVencimiento estrategia = new SegmentarPorVencimiento();
        List<List<Bien>> resultado = estrategia.segmentar(input);

        assertEquals(1, resultado.size());
        assertEquals(2, resultado.get(0).size());
    }

    @Test
    public void testSegmentarPorVencimientoSeparaPorFecha() {
        Subcategoria sub = new Subcategoria("Leche", "Leche");
        LocalDate fecha1 = LocalDate.of(2026, 9, 1);
        LocalDate fecha2 = LocalDate.of(2026, 10, 1);

        Bien bien1 = crearBien("Leche A", sub, fecha1, null);
        Bien bien2 = crearBien("Leche B", sub, fecha1, null);
        Bien bien3 = crearBien("Leche C", sub, fecha2, null);

        List<List<Bien>> input = new ArrayList<>();
        input.add(List.of(bien1, bien2, bien3));

        SegmentarPorVencimiento estrategia = new SegmentarPorVencimiento();
        List<List<Bien>> resultado = estrategia.segmentar(input);

        // Debe separar en 2 grupos por fecha de vencimiento
        assertEquals(2, resultado.size());
    }

    // ========================= SegmentarPorEstado =========================

    @Test
    public void testSegmentarPorEstadoSinEstadoPasaIntacto() {
        Subcategoria sub = new Subcategoria("Ropa", "Ropa");

        Bien bien1 = crearBien("Campera", sub, null, null);
        Bien bien2 = crearBien("Pantalón", sub, null, null);

        List<List<Bien>> input = new ArrayList<>();
        input.add(List.of(bien1, bien2));

        SegmentarPorEstado estrategia = new SegmentarPorEstado();
        List<List<Bien>> resultado = estrategia.segmentar(input);

        assertEquals(1, resultado.size());
        assertEquals(2, resultado.get(0).size());
    }

    @Test
    public void testSegmentarPorEstadoSeparaUsadosDeNuevos() {
        Subcategoria sub = new Subcategoria("Ropa", "Ropa");

        Bien bienUsado = crearBien("Campera usada", sub, null, true);
        Bien bienNuevo = crearBien("Campera nueva", sub, null, false);

        List<List<Bien>> input = new ArrayList<>();
        input.add(List.of(bienUsado, bienNuevo));

        SegmentarPorEstado estrategia = new SegmentarPorEstado();
        List<List<Bien>> resultado = estrategia.segmentar(input);

        // Debe separar en 2 grupos: usados y nuevos
        assertEquals(2, resultado.size());
    }

    // ========================= Helpers =========================

    private Bien crearBien(String descripcion, Subcategoria sub, LocalDate fechaVencimiento, Boolean esUsado) {
        Bien bien = new Bien(descripcion, 1.0, "unidad", esUsado != null ? esUsado : false, fechaVencimiento);
        bien.setSubcategoria(sub);
        if (esUsado != null) {
            bien.setEsUsado(esUsado);
        } else {
            bien.setEsUsado(null);
        }
        return bien;
    }
}
