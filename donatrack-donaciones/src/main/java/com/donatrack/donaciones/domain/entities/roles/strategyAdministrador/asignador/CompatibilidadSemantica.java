package com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.asignador;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.necesidades.Necesidad;
import com.donatrack.donaciones.domain.services.ResultadoMatch;

import java.util.ArrayList;
import java.util.List;

public class CompatibilidadSemantica implements AlgoritmoAsignacion {

    @Override
    public List<ResultadoMatch> recomendarNecesidades(List<Donacion> donaciones, List<Necesidad> necesidades) {
        List<ResultadoMatch> matches = new ArrayList<>();
        
        for (Donacion donacion : donaciones) {
            for (Necesidad necesidad : necesidades) {
                if (donacion.getSubCategoria() != null && 
                    necesidad.getSubcategoriaRequerida() != null &&
                    donacion.getSubCategoria().equals(necesidad.getSubcategoriaRequerida())) {
                    
                    matches.add(new ResultadoMatch(donacion, null, necesidad));
                    break; // Una donación se asigna a un match
                }
            }
        }
        return matches;
    }
}
