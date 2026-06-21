package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepository;
import com.donatrack.logistica.domain.entities.RutaDeReparto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MockRutaDeRepartoRepository implements RutaDeRepartoRepository {

    private final List<RutaDeReparto> baseDeDatosMock = new ArrayList<>();

    @Override
    public void guardar(RutaDeReparto ruta) {
        baseDeDatosMock.removeIf(r -> r.getId().equals(ruta.getId()));
        baseDeDatosMock.add(ruta);
    }

    @Override
    public Optional<RutaDeReparto> buscarPorId(UUID id) {
        return baseDeDatosMock.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<RutaDeReparto> buscarTodas() {
        return new ArrayList<>(baseDeDatosMock);
    }
}
