package com.donatrack.donaciones.domain.repository;

import com.donatrack.donaciones.domain.entities.donacion.RecepcionDonacion;
import java.util.Optional;
import java.util.UUID;

public interface RecepcionDonacionRepository {
    void guardar(RecepcionDonacion recepcion);
    Optional<RecepcionDonacion> buscarPorId(UUID id);
}
