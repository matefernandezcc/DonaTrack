package com.donatrack.logistica.application.ports.out;

import java.util.Optional;
import java.util.UUID;

import com.donatrack.logistica.domain.entities.entregas.Entrega;

public interface EntregaRepositoryPort {
    Optional<Entrega> buscarPorId(UUID idEntrega);
    void guardar(Entrega entrega);
}
