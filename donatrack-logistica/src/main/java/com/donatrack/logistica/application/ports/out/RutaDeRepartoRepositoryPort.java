package com.donatrack.logistica.application.ports.out;

import com.donatrack.logistica.domain.entities.RutaDeReparto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RutaDeRepartoRepositoryPort {
    Optional<RutaDeReparto> buscarPorIdDonacion(UUID idDonacion);
    Optional<RutaDeReparto> buscarPorId(UUID idRuta);
    List<RutaDeReparto> obtenerTodas();
    void guardar(RutaDeReparto ruta);
}
