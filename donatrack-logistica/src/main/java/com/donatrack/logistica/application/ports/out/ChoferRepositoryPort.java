package com.donatrack.logistica.application.ports.out;

import com.donatrack.logistica.domain.entities.reparto.Chofer;
import java.util.List;
import java.util.Optional;

public interface ChoferRepositoryPort {
    void guardar(Chofer chofer);
    Optional<Chofer> buscarPorLegajo(String legajo);
    List<Chofer> obtenerTodos();
    void eliminar(String legajo);
}
