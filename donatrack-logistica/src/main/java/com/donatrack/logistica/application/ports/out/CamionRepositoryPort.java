package com.donatrack.logistica.application.ports.out;

import com.donatrack.logistica.domain.entities.Camion;
import java.util.List;
import java.util.Optional;

public interface CamionRepositoryPort {
    void guardar(Camion camion);

    Optional<Camion> buscarPorPatente(String patente);

    List<Camion> obtenerTodos();

    void eliminar(String patente);
}
