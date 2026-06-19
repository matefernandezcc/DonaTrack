package com.donatrack.incentivos.domain.service;

import com.donatrack.incentivos.domain.model.PerfilDonante;
import com.donatrack.incentivos.domain.model.ranking.RankingPuntuacionStrategy;
import com.donatrack.incentivos.domain.repository.PerfilDonanteRepository;

import java.util.List;
import java.util.stream.Collectors;

public class RankingMensualService {

    private final PerfilDonanteRepository repository;
    private final RankingPuntuacionStrategy puntuacionStrategy;

    public RankingMensualService(PerfilDonanteRepository repository, RankingPuntuacionStrategy puntuacionStrategy) {
        this.repository = repository;
        this.puntuacionStrategy = puntuacionStrategy;
    }

    public List<PerfilDonante> obtenerTop5Mensual() {
        List<PerfilDonante> todosLosPerfiles = repository.findAll();

        return todosLosPerfiles.stream()
            .sorted((p1, p2) -> Double.compare(puntuacionStrategy.calcularPuntaje(p2), puntuacionStrategy.calcularPuntaje(p1))) // Orden descendente
            .limit(5)
            .collect(Collectors.toList());
    }
}
