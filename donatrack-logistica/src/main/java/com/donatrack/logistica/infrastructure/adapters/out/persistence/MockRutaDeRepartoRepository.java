package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.domain.entities.reparto.Parada;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MockRutaDeRepartoRepository implements RutaDeRepartoRepositoryPort {

    private final List<RutaDeReparto> baseDeDatosMock = new ArrayList<>();

    @Override
    public Optional<RutaDeReparto> buscarPorIdDonacion(UUID idDonacion) {
        for (RutaDeReparto ruta : baseDeDatosMock) {
            if (ruta.getParadas() != null) {
                for (Parada parada : ruta.getParadas()) {
                    if (parada.getEntregas() != null) {
                        for (Entrega entrega : parada.getEntregas()) {
                            if (entrega.getIdEntrega().equals(idDonacion)) {
                                return Optional.of(ruta);
                            }
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void guardar(RutaDeReparto ruta) {
        baseDeDatosMock.removeIf(r -> r.getId().equals(ruta.getId()));
        baseDeDatosMock.add(ruta);
    }
}
