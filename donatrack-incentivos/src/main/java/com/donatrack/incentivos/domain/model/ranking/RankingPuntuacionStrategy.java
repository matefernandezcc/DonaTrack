package com.donatrack.incentivos.domain.model.ranking;

import com.donatrack.incentivos.domain.model.PerfilDonante;

public interface RankingPuntuacionStrategy {
    /**
     * Calcula el puntaje de un donante basado en sus métricas y categoría.
     * @param perfil Perfil a evaluar
     * @return Puntaje total para el ranking
     */
    double calcularPuntaje(PerfilDonante perfil);
}
