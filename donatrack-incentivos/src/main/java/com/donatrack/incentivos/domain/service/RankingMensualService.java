package com.donatrack.incentivos.domain.service;

import com.donatrack.incentivos.domain.entities.PerfilDonante;
import com.donatrack.incentivos.domain.entities.ranking.RankingStrategy;
import com.donatrack.incentivos.domain.repository.PerfilDonanteRepository;

import java.util.List;
import java.time.YearMonth;
import java.util.stream.Collectors;

public class RankingMensualService {

    private final PerfilDonanteRepository repository;
    private final RankingStrategy puntuacionStrategy;

    public RankingMensualService(PerfilDonanteRepository repository, RankingStrategy puntuacionStrategy) {
        this.repository = repository;
        this.puntuacionStrategy = puntuacionStrategy;
    }

    public List<PerfilDonante> obtenerTop3Mensual(YearMonth mes) {
        List<PerfilDonante> todosLosPerfiles = repository.findAll();

        return todosLosPerfiles.stream()
                .sorted((p1, p2) -> Double.compare(puntuacionStrategy.calcularPuntaje(p2, mes),
                        puntuacionStrategy.calcularPuntaje(p1, mes))) // Orden descendente
                .limit(3)
                .collect(Collectors.toList());
    }
}
