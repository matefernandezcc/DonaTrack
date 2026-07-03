package com.donatrack.donaciones.domain.services;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.necesidades.Necesidad;
import com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.asignador.*;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MatchmakerService {

    private final AlgoritmoAsignacion algoritmoPrimario;
    private final AlgoritmoAsignacion algoritmoSecundario;

    public MatchmakerService() {
        this.algoritmoPrimario = new CompatibilidadSemantica();
        this.algoritmoSecundario = new PrioridadASubAtendidos();
    }
    
    public MatchmakerService(AlgoritmoAsignacion algoritmoPrimario, AlgoritmoAsignacion algoritmoSecundario) {
        this.algoritmoPrimario = algoritmoPrimario;
        this.algoritmoSecundario = algoritmoSecundario;
    }

    public List<Beneficiario> obtenerSugerencias(Donacion donacion, List<Beneficiario> disponibles) {
        List<Necesidad> necesidades = disponibles.stream()
                .flatMap(b -> b.getNecesidadesDeclaradas().stream())
                .collect(Collectors.toList());

        List<Donacion> donaciones = List.of(donacion);

        List<ResultadoMatch> matchesPrimarios = algoritmoPrimario.recomendarNecesidades(donaciones, necesidades);
        List<ResultadoMatch> matchesSecundarios = algoritmoSecundario.recomendarNecesidades(donaciones, necesidades);

        // Mapear los matches de necesidades de vuelta a sus beneficiarios declarantes
        List<Beneficiario> sugerenciasPrimarias = buscarBeneficiariosDeNecesidades(matchesPrimarios, disponibles);
        List<Beneficiario> sugerenciasSecundarias = buscarBeneficiariosDeNecesidades(matchesSecundarios, disponibles);

        Set<Beneficiario> interseccion = sugerenciasPrimarias.stream()
                .filter(sugerenciasSecundarias::contains)
                .collect(Collectors.toSet());

        if (!interseccion.isEmpty()) {
            return List.copyOf(interseccion);
        } else {
            Set<Beneficiario> union = sugerenciasPrimarias.stream().collect(Collectors.toSet());
            union.addAll(sugerenciasSecundarias);
            return List.copyOf(union);
        }
    }

    private List<Beneficiario> buscarBeneficiariosDeNecesidades(List<ResultadoMatch> matches, List<Beneficiario> disponibles) {
        List<Beneficiario> resultado = new ArrayList<>();
        for (ResultadoMatch match : matches) {
            for (Beneficiario b : disponibles) {
                if (b.getNecesidadesDeclaradas().contains(match.getNecesidad())) {
                    if (!resultado.contains(b)) {
                        resultado.add(b);
                    }
                }
            }
        }
        return resultado;
    }
}
