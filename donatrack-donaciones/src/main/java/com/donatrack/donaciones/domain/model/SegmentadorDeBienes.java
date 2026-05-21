package com.donatrack.donaciones.domain.model;

import com.donatrack.donaciones.domain.enums.EstadoDonacion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SegmentadorDeBienes {

    List<EstrategiaSegmentacion> estrategiasSegmentacion = new ArrayList<>(); //Patron pipes and filters

    public SegmentadorDeBienes() {
        //Pipeline, cada aplicacion de filtro se aplica en orden
        this.estrategiasSegmentacion.add(new SegmentarPorSubcategoria());
        this.estrategiasSegmentacion.add(new SegmentarPorVencimiento());
        this.estrategiasSegmentacion.add(new SegmentarPorEstado());
    }

    public List<Donacion> procesar(List<Bien> bienesBrutos) {
        //1. Lista inicial
        List<List<Bien>> listasDeBienes = new ArrayList<>();
        listasDeBienes.add(bienesBrutos);

        //2. Se ejecuta el pipeline
        for (EstrategiaSegmentacion estrategia : estrategiasSegmentacion) {
            listasDeBienes = estrategia.segmentar(listasDeBienes);
        }

        //3. Cada lista de bienes se convierte en una Donacion
        List<Donacion> donacionesResultantes = new ArrayList<>();

        for (List<Bien> listaFinal : listasDeBienes) {
            if (listaFinal.isEmpty()) {
                continue;
            }
            //bien de muestra para asignarle la misma subcategoria a la donacion
            Bien muestra = listaFinal.get(0);

            //Instanciamos la Donacion y le asignamos la subcategoria en base a la muestra
            Donacion nuevaDonacion = new Donacion(muestra.getSubcategoria());
            nuevaDonacion.setId(UUID.randomUUID());

            //Si no es perecedero, la fecha de vencimiento es null
            nuevaDonacion.setFechaVencimiento(muestra.getFechaVencimiento());

            nuevaDonacion.setBienes(listaFinal);
            //Estado inicial de donacion: Pendiente
            nuevaDonacion.setEstado(EstadoDonacion.PENDIENTE);

            donacionesResultantes.add(nuevaDonacion);
        }

        return donacionesResultantes;
    }
}