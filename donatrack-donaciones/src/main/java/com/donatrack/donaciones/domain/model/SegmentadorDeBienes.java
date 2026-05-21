package com.donatrack.donaciones.domain.model;

import java.util.ArrayList;
import java.util.List;

public class SegmentadorDeBienes {

    List<EstrategiaSegmentacion> estrategiasSegmentacion = new ArrayList<>(); //Patron pipes and filters

    public SegmentadorDeBienes() {
        //Pipeline, cada aplicacion de filtro se aplica en orden
        this.estrategiasSegmentacion.add(new SegmentarPorSubcategoria());
        this.estrategiasSegmentacion.add(new SegmentarPorVencimiento());
        this.estrategiasSegmentacion.add(new SegmentarPorEstado());
    }

    //La idea es hacer un bucle, y e ir aplicando filtros a las listas de bienes (las futuras donaciones)
    public List<Donacion> procesar(List<Bien> bienesBrutos) {
        //1. Las estrategias de segmentacion reciben List<List<Bien>> y devuelven lo mismo
        // List<List<Bien>> = una futura List<Donacion> (ya que Donacion tambien tiene lista de bienes)
        List<List<Bien>> bienesBrutosUnprocessed = new ArrayList<>();
        bienesBrutosUnprocessed.add(bienesBrutos);

        for (EstrategiaSegmentacion estrategia : estrategiasSegmentacion) {
        }

        return null;
    }
}