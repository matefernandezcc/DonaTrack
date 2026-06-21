package com.donatrack.logistica.application.ports.out;

import com.donatrack.logistica.domain.entities.Camion;
import java.util.List;
import java.util.Optional;

public interface CamionRepository {
    void guardar(Camion camion);
    Optional<Camion> buscarPorPatente(String patente);
    List<Camion> buscarTodos();
}
