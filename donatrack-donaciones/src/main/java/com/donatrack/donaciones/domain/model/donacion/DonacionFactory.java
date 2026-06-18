package com.donatrack.donaciones.domain.model.donacion;

import java.util.List;

public class DonacionFactory {

    public Donacion crearDesdeBienes(List<Bien> bienes) {
        if (bienes == null || bienes.isEmpty()) {
            return null;
        }

        // bien de muestra para asignarle la misma subcategoria a la donacion
        Bien muestra = bienes.get(0);

        // Instanciamos la Donacion y le asignamos la subcategoria en base a la muestra
        Donacion nuevaDonacion = new Donacion(muestra.getSubcategoria());

        // Si no es perecedero, la fecha de vencimiento es null
        nuevaDonacion.setFechaVencimiento(muestra.getFechaVencimiento());

        nuevaDonacion.setBienes(bienes);

        return nuevaDonacion;
    }
}
