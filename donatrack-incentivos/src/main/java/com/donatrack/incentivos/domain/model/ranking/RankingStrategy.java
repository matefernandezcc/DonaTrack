package com.donatrack.incentivos.domain.model.ranking;

import com.donatrack.incentivos.domain.model.PerfilDonante;
import java.time.YearMonth;

public interface RankingStrategy {
    /**
     * Calcula el puntaje de un donante basado en las misiones cumplidas en un mes.
     * 
     * @param perfil Perfil a evaluar
     * @param mes    Mes a evaluar
     * @return Puntaje total para el ranking
     */
    double calcularPuntaje(PerfilDonante perfil, YearMonth mes);
}
