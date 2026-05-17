package com.donatrack.donaciones.domain.model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SegmentarPorVencimiento implements EstrategiaSegmentacion {

    @Override
    public List<Donacion> segmentar(List<Bien> bienes) {
        // Agrupamos utilizando la fecha de vencimiento como clave
        Map<LocalDate, Donacion> donacionesPorVencimiento = new HashMap<>();

        for (Bien bien : bienes) {
            LocalDate vencimiento = bien.getFechaVencimiento();
            
            if (!donacionesPorVencimiento.containsKey(vencimiento)) {
                // Al crear la donación, le asignamos la subcategoría del primer bien que entra
                donacionesPorVencimiento.put(vencimiento, new Donacion(bien.getSubcategoria()));
            }
            
            donacionesPorVencimiento.get(vencimiento).agregarBien(bien);
        }

        return new ArrayList<>(donacionesPorVencimiento.values());
    }
}