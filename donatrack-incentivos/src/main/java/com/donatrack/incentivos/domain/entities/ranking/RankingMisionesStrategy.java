package com.donatrack.incentivos.domain.entities.ranking;

import com.donatrack.incentivos.domain.entities.PerfilDonante;

import java.time.YearMonth;

public class RankingMisionesStrategy implements RankingStrategy {

    @Override
    public double calcularPuntaje(PerfilDonante perfil, YearMonth mes) {
        return perfil.getMetricas().totalMisionesCompletadasEn(mes);
    }
}
