package com.donatrack.logistica.application.ports.out;

import com.donatrack.logistica.domain.entities.Chofer;
import java.util.List;
import java.util.Optional;

public interface ChoferRepository {
    void guardar(Chofer chofer);
    Optional<Chofer> buscarPorLegajo(String legajo);
    List<Chofer> buscarTodos();
}
