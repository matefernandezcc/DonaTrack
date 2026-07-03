package com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.asignador;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.necesidades.Necesidad;
import com.donatrack.donaciones.domain.services.ResultadoMatch;

import java.util.ArrayList;
import java.util.List;

public class PrioridadASubAtendidos implements AlgoritmoAsignacion {

    @Override
    public List<ResultadoMatch> recomendarNecesidades(List<Donacion> donaciones, List<Necesidad> necesidades) {
        List<ResultadoMatch> matches = new ArrayList<>();
        
        // Asignación simple por orden de lista (para convalidar el tipo de retorno)
        int minSize = Math.min(donaciones.size(), necesidades.size());
        for (int i = 0; i < minSize; i++) {
            matches.add(new ResultadoMatch(donaciones.get(i), null, necesidades.get(i)));
        }
        return matches;
    }
}
