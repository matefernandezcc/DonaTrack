package com.donatrack.incentivos.infrastructure.controller;

import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetricasDonanteDTO {
    private UUID donanteId;
    private int totalDonacionesHistoricas;
    private int mesesConsecutivosDonando;
    private int cantidadBienesDonados;
    private int donacionesExitosas;
    private int totalOrganizacionesAyudadas;
    private Map<YearMonth, Integer> historialDonacionesPorMes;
    // mock ranking
    private int posicionRanking; 
}
