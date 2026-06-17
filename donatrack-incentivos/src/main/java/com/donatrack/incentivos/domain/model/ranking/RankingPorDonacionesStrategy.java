package com.donatrack.incentivos.domain.model.ranking;

import com.donatrack.incentivos.domain.model.PerfilDonante;

public class RankingPorDonacionesStrategy implements RankingPuntuacionStrategy {

    @Override
    public double calcularPuntaje(PerfilDonante perfil) {
        double puntaje = 0;
        
        // Cada donación exitosa vale 10 puntos
        puntaje += perfil.getDonacionesExitosas() * 10;
        
        // Cada bien donado suma 1 punto
        puntaje += perfil.getCantidadBienesDonados() * 1;
        
        // Bonus por racha de meses consecutivos (5 puntos por mes)
        puntaje += perfil.getMesesConsecutivosDonando() * 5;

        // Multiplicador según categoría
        double multiplicador = switch (perfil.getCategoria().getValorEnum()) {
            case COLABORADOR -> 1.0;
            case SOSTENEDOR -> 1.2;
            case TRANSFORMADOR -> 1.5;
        };

        return puntaje * multiplicador;
    }
}
