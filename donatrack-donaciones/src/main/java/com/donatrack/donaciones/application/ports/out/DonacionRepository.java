package com.donatrack.donaciones.application.ports.out;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;

import java.util.List;

public interface DonacionRepository {
    List<Donacion> buscarPorEstado(EstadoDonacion estado);
    java.util.Optional<Donacion> buscarPorId(java.util.UUID id);
    void guardar(Donacion donacion);
}
