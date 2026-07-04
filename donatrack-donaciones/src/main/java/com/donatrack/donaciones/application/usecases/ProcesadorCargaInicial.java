package com.donatrack.donaciones.application.usecases;

import com.donatrack.donaciones.domain.entities.donacion.Bien;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.DonacionFactory;
import com.donatrack.donaciones.domain.entities.donacion.segmentador.EstrategiaSegmentacion;

import java.util.ArrayList;
import java.util.List;

public class ProcesadorCargaInicial {

    private final List<EstrategiaSegmentacion> estrategiasSegmentacion;
    private final DonacionFactory donacionFactory;

    public ProcesadorCargaInicial(List<EstrategiaSegmentacion> estrategiasSegmentacion, DonacionFactory donacionFactory) {
        this.estrategiasSegmentacion = estrategiasSegmentacion;
        this.donacionFactory = donacionFactory;
    }

    public List<Donacion> procesar(List<Bien> bienesBrutos) {
        // 1. Lista inicial
        List<List<Bien>> listasDeBienes = new ArrayList<>();
        listasDeBienes.add(bienesBrutos);

        // 2. Se ejecuta el pipeline
        for (EstrategiaSegmentacion estrategia : estrategiasSegmentacion) {
            listasDeBienes = estrategia.segmentar(listasDeBienes);
        }

        // 3. Cada lista de bienes se convierte en una Donacion
        List<Donacion> donacionesResultantes = new ArrayList<>();
        for (List<Bien> listaFinal : listasDeBienes) {
            Donacion nuevaDonacion = donacionFactory.crearDesdeBienes(listaFinal);
            if (nuevaDonacion != null) {
                donacionesResultantes.add(nuevaDonacion);
            }
        }

        return donacionesResultantes;
    }
}
