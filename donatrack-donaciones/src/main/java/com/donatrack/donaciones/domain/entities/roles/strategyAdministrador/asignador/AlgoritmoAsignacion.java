package com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.asignador;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.necesidades.Necesidad;
import com.donatrack.donaciones.domain.services.ResultadoMatch;
import java.util.List;

public interface AlgoritmoAsignacion {
    List<ResultadoMatch> recomendarNecesidades(List<Donacion> donaciones, List<Necesidad> necesidades);
}
