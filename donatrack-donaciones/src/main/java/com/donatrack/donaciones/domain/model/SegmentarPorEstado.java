package com.donatrack.donaciones.domain.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SegmentarPorEstado implements EstrategiaSegmentacion {

    @Override public List<Donacion> segmentar(List<Bien> bienes) {
        // Usamos un booleano como clave: true (esUsado) o false (es nuevo)
        Map<Boolean, Donacion> donacionesPorEstado = new HashMap<>();

        for (Bien bien : bienes) {
            boolean estadoUsado = bien.isEsUsado();
            
            if (!donacionesPorEstado.containsKey(estadoUsado)) {
                donacionesPorEstado.put(estadoUsado, new Donacion(bien.getSubcategoria()));
            }
            
            donacionesPorEstado.get(estadoUsado).agregarBien(bien);
        }

        return new ArrayList<>(donacionesPorEstado.values());
    }
}