package com.donatrack.incentivos.domain.model;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.time.YearMonth;
import com.donatrack.common.dto.ActividadDonacionDTO;

@Getter
@Setter
public class PerfilDonante {
    private UUID donanteId;
    private CategoriaDonante categoria;
    private List<Insignia> insigniasObtenidas;
    private List<Mision> misionesActivas;
    
    // Métricas
    private int totalDonacionesHistoricas;
    private int mesesConsecutivosDonando;
    private int cantidadBienesDonados;
    private int donacionesExitosas;
    
    // Historial para gráficas y analíticas
    private Set<UUID> organizacionesUnicasAyudadas;
    private Map<YearMonth, Integer> historialDonacionesPorMes;
    private YearMonth ultimoMesDonacion;

    public PerfilDonante(UUID donanteId) {
        this.donanteId = donanteId;
        this.categoria = CategoriaDonante.COLABORADOR;
        this.insigniasObtenidas = new ArrayList<>();
        this.misionesActivas = new ArrayList<>();
        this.organizacionesUnicasAyudadas = new HashSet<>();
        this.historialDonacionesPorMes = new HashMap<>();
    }

    public void registrarDonacionExitosa(ActividadDonacionDTO actividad) {
        this.donacionesExitosas++;
        this.totalDonacionesHistoricas++;
        this.cantidadBienesDonados += actividad.getCantidadBienes();
        
        if (actividad.getIdEntidadBeneficiaria() != null) {
            this.organizacionesUnicasAyudadas.add(actividad.getIdEntidadBeneficiaria());
        }

        YearMonth mesActual = YearMonth.from(actividad.getFecha());
        historialDonacionesPorMes.put(mesActual, historialDonacionesPorMes.getOrDefault(mesActual, 0) + 1);

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

        evaluarMisiones();
    }

    public void evaluarMisiones() {
        List<Mision> completadas = new ArrayList<>();
        for (Mision mision : misionesActivas) {
            if (mision.evaluar(this)) {
                completadas.add(mision);
                agregarInsignia(mision.getRecompensa());
            }
        }
        misionesActivas.removeAll(completadas);
    }

    private void agregarInsignia(Insignia insignia) {
        if (insignia != null) {
            this.insigniasObtenidas.add(insignia);
        }
    }
}
