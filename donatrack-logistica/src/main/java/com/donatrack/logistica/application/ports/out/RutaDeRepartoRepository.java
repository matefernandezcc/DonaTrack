package com.donatrack.logistica.application.ports.out;

import com.donatrack.logistica.domain.entities.RutaDeReparto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RutaDeRepartoRepository {
    void guardar(RutaDeReparto ruta);
    Optional<RutaDeReparto> buscarPorId(UUID id);
    List<RutaDeReparto> buscarTodas();
}
