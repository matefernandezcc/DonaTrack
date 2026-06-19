package com.donatrack.donaciones.domain.repository;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.enums.EstadoDonacion;

import java.util.List;

public interface DonacionRepository {
    List<Donacion> buscarPorEstado(EstadoDonacion estado);
    java.util.Optional<Donacion> buscarPorId(java.util.UUID id);
    void guardar(Donacion donacion);
}
