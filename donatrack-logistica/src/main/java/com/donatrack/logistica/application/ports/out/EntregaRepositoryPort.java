package com.donatrack.logistica.application.ports.out;

import com.donatrack.logistica.domain.entities.Entrega;
import java.util.Optional;
import java.util.UUID;

public interface EntregaRepositoryPort {
    Optional<Entrega> buscarPorId(UUID idEntrega);
    void guardar(Entrega entrega);
}
