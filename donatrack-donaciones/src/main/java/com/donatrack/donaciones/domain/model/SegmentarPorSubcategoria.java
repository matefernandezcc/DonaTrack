package com.donatrack.donaciones.domain.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SegmentarPorSubcategoria implements EstrategiaSegmentacion {

    @Override
    public List<Donacion> segmentar(List<Bien> bienes) {
        // Usamos un Map para agrupar las donaciones utilizando la Subcategoria como clave
        Map<Categoria, Donacion> donacionesPorSubcategoria = new HashMap<>();

        for (Bien bien : bienes) {
            Categoria sub = bien.getSubcategoria();
            
            // Si todavía no creamos una Donación para esta subcategoría, la instanciamos
            if (!donacionesPorSubcategoria.containsKey(sub)) {
                donacionesPorSubcategoria.put(sub, new Donacion(sub));
            }
            
            // Agregamos el bien a la donación que le corresponde
            donacionesPorSubcategoria.get(sub).agregarBien(bien);
        }

        // Devolvemos solo los valores del mapa (la lista de donaciones ya armadas)
        return new ArrayList<>(donacionesPorSubcategoria.values());
    }
}