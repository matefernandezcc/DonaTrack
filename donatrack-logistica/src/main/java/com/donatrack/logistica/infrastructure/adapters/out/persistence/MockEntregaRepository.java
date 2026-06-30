package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.EntregaRepositoryPort;
import com.donatrack.logistica.domain.entities.Entrega;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MockEntregaRepository implements EntregaRepositoryPort {

    private final List<Entrega> baseDeDatosMock = new ArrayList<>();

    @Override
    public Optional<Entrega> buscarPorId(UUID idEntrega) {
        return baseDeDatosMock.stream()
                .filter(e -> e.getIdEntrega().equals(idEntrega))
                .findFirst();
    }

    @Override
    public void guardar(Entrega entrega) {
        baseDeDatosMock.removeIf(e -> e.getIdEntrega().equals(entrega.getIdEntrega()));
        baseDeDatosMock.add(entrega);
    }
}
