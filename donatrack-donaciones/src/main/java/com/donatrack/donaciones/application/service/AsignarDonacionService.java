package com.donatrack.donaciones.application.service;

import com.donatrack.donaciones.application.usecase.AsignarDonacionUseCase;
import com.donatrack.donaciones.domain.model.Administrador;
import com.donatrack.donaciones.domain.model.Donacion;

public class AsignarDonacionService implements AsignarDonacionUseCase {

    // private DonacionRepository donacionRepo;
    // private NecesidadRepository necesidadRepo;

    @Override
    public void ejecutar(Donacion donacion, Administrador administrador) {
        // List<Necesidad> necesidades = necesidadRepo.buscarPendientes();
        // administrador.asignarDonacion(donacion, necesidades);
        // donacionRepo.guardar(donacion);
    }
}
