package com.donatrack.incentivos.domain.model;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public PerfilDonante(UUID donanteId) {
        this.donanteId = donanteId;
        this.categoria = CategoriaDonante.COLABORADOR;
        this.insigniasObtenidas = new ArrayList<>();
        this.misionesActivas = new ArrayList<>();
    }

    public void registrarDonacionExitosa() {
        this.donacionesExitosas++;
        this.totalDonacionesHistoricas++;
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
