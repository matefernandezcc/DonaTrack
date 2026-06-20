package com.donatrack.incentivos.domain.model.ranking;

import com.donatrack.incentivos.domain.model.PerfilDonante;

import java.time.YearMonth;

public class RankingMisionesStrategy implements RankingStrategy {

    @Override
    public double calcularPuntaje(PerfilDonante perfil, YearMonth mes) {
        return perfil.getMetricas().getMisionesCompletadasEn(mes);
    }
}
