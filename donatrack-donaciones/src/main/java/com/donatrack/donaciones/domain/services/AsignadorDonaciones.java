package com.donatrack.donaciones.domain.services;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.necesidades.Necesidad;
import com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.asignador.AlgoritmoAsignacion;

import java.util.List;

public class AsignadorDonaciones {

    private final AlgoritmoAsignacion algoritmoAsignacion;

    public AsignadorDonaciones(AlgoritmoAsignacion algoritmoAsignacion) {
        this.algoritmoAsignacion = algoritmoAsignacion;
    }

    public List<ResultadoMatch> ejecutarMatchmaking(List<Donacion> donaciones, List<Necesidad> necesidades) {
        return algoritmoAsignacion.recomendarNecesidades(donaciones, necesidades);
    }
}
