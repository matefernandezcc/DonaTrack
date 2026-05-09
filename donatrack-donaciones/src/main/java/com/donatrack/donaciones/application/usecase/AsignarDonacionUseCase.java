package com.donatrack.donaciones.application.usecase;

import com.donatrack.donaciones.domain.model.Administrador;
import com.donatrack.donaciones.domain.model.Donacion;

public interface AsignarDonacionUseCase {
    void ejecutar(Donacion donacion, Administrador administrador);
}