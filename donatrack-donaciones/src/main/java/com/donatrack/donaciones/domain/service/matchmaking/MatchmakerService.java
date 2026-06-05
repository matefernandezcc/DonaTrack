package com.donatrack.donaciones.domain.service.matchmaking;

import com.donatrack.donaciones.domain.model.Beneficiario;
import com.donatrack.donaciones.domain.model.Donacion;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
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

    /**
     * Ejecuta ambos algoritmos y retorna las entidades que coinciden en ambos rankings.
     * Si no hay coincidencias, puede retornar los resultados de ambos (o la unión, dependiendo de la política).
     * En la consigna dice: "filtrar automáticamente las entidades que hayan aparecido en la ejecución de ambos... 
     * Si no hubo coincidencias, entonces mostrará ambas ejecuciones."
     */
    public List<Beneficiario> obtenerSugerencias(Donacion donacion, List<Beneficiario> disponibles) {
        List<Beneficiario> sugerenciasPrimarias = algoritmoPrimario.sugerirBeneficiarios(donacion, disponibles);
        List<Beneficiario> sugerenciasSecundarias = algoritmoSecundario.sugerirBeneficiarios(donacion, disponibles);

        Set<Beneficiario> interseccion = sugerenciasPrimarias.stream()
                .filter(sugerenciasSecundarias::contains)
                .collect(Collectors.toSet());

        if (!interseccion.isEmpty()) {
            return List.copyOf(interseccion);
        } else {
            // Unir ambos sin duplicados
            Set<Beneficiario> union = sugerenciasPrimarias.stream().collect(Collectors.toSet());
            union.addAll(sugerenciasSecundarias);
            return List.copyOf(union);
        }
    }
}
