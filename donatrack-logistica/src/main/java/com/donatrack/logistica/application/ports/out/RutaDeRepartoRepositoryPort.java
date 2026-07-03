package com.donatrack.logistica.application.ports.out;

import java.util.Optional;
import java.util.UUID;

import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;

public interface RutaDeRepartoRepositoryPort {
    Optional<RutaDeReparto> buscarPorIdDonacion(UUID idDonacion);
    void guardar(RutaDeReparto ruta);
}
