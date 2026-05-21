package com.donatrack.donaciones.domain.strategy;
import com.donatrack.donaciones.domain.model.Donacion;
import com.donatrack.donaciones.domain.model.Necesidad;
import java.util.List;

public class PorCercania implements AsignacionStrategy {
    @Override public List<Donacion> recomendarNecesidades(List<Donacion> donaciones, List<Necesidad> necesidades) {
        return null; 
    }
}
