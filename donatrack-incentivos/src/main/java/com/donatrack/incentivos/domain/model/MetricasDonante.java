package com.donatrack.incentivos.domain.model;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;

@Getter
public class MetricasDonante {

    private int totalDonacionesHistoricas;
    private int mesesConsecutivosDonando;
    private int cantidadBienesDonados;
    private int donacionesExitosas;
    private int maxBienesEnUnaDonacion;
    private Set<String> categoriasUnicasDonadas;
    private Set<UUID> organizacionesUnicasAyudadas;
    private Map<YearMonth, Integer> historialDonacionesPorMes;
    private YearMonth ultimoMesDonacion;

    public MetricasDonante() {
        this.totalDonacionesHistoricas = 0;
        this.mesesConsecutivosDonando = 0;
        this.cantidadBienesDonados = 0;
        this.donacionesExitosas = 0;
        this.maxBienesEnUnaDonacion = 0;
        this.categoriasUnicasDonadas = new HashSet<>();
        this.organizacionesUnicasAyudadas = new HashSet<>();
        this.historialDonacionesPorMes = new HashMap<>();
        this.ultimoMesDonacion = null;
    }

    public void registrarDonacionExitosa(int cantidadBienes, Set<String> categorias, UUID idEntidadBeneficiaria, YearMonth mesDonacion) {
        this.donacionesExitosas++;
        this.totalDonacionesHistoricas++;
        this.cantidadBienesDonados += cantidadBienes;

        if (idEntidadBeneficiaria != null) {
            this.organizacionesUnicasAyudadas.add(idEntidadBeneficiaria);
        }

        if (cantidadBienes > this.maxBienesEnUnaDonacion) {
            this.maxBienesEnUnaDonacion = cantidadBienes;
        }

        if (categorias != null && !categorias.isEmpty()) {
            this.categoriasUnicasDonadas.addAll(categorias);
        }

        historialDonacionesPorMes.put(mesDonacion, historialDonacionesPorMes.getOrDefault(mesDonacion, 0) + 1);

        actualizarRacha(mesDonacion);
    }

    private void actualizarRacha(YearMonth mesActual) {
        if (ultimoMesDonacion == null || ultimoMesDonacion.plusMonths(1).equals(mesActual)) {
            // Aumenta racha si es el primer mes o es un mes exactamente posterior
            if (ultimoMesDonacion != null && !ultimoMesDonacion.equals(mesActual)) {
                this.mesesConsecutivosDonando++;
            } else if (ultimoMesDonacion == null) {
                this.mesesConsecutivosDonando = 1;
            }
        } else if (mesActual.isAfter(ultimoMesDonacion.plusMonths(1))) {
            // Perdió la racha
            this.mesesConsecutivosDonando = 1;
        }
        this.ultimoMesDonacion = mesActual;
    }
}
