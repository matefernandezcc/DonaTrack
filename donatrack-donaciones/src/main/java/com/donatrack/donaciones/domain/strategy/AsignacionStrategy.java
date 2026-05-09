package com.donatrack.donaciones.domain.strategy;

import java.util.List;

import com.donatrack.donaciones.domain.model.Donacion;
import com.donatrack.donaciones.domain.model.Necesidad;

public interface AsignacionStrategy {
    void asignar(Donacion donacion, List<Necesidad> necesidades);
}
