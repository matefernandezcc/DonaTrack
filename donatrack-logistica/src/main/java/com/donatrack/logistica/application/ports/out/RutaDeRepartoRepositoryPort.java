package com.donatrack.logistica.application.ports.out;

import com.donatrack.logistica.domain.entities.RutaDeReparto;
import java.util.Optional;
import java.util.UUID;

public interface RutaDeRepartoRepositoryPort {
    Optional<RutaDeReparto> buscarPorIdDonacion(UUID idDonacion);
    void guardar(RutaDeReparto ruta);
}
