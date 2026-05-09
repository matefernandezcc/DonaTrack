package com.donatrack.donaciones.domain.model;

import java.util.List;

import com.donatrack.donaciones.domain.strategy.AsignacionStrategy;

public class Administrador {
    private AsignacionStrategy estrategia;

    public void asignarDonacion(Donacion d, List<Necesidad> necesidades) {
        estrategia.asignar(d, necesidades);
    }
}
