package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.ChoferRepository;
import com.donatrack.logistica.domain.entities.Chofer;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MockChoferRepository implements ChoferRepository {

    private final List<Chofer> baseDeDatosMock = new ArrayList<>();

    public MockChoferRepository() {
        // Inicializar con algunos choferes mock
        baseDeDatosMock.add(new Chofer("L-001", "Gómez Juan"));
        baseDeDatosMock.add(new Chofer("L-002", "Pérez Carlos"));
        baseDeDatosMock.add(new Chofer("L-003", "Rodríguez Mario"));
    }

    @Override
    public void guardar(Chofer chofer) {
        baseDeDatosMock.removeIf(c -> c.getLegajo().equalsIgnoreCase(chofer.getLegajo()));
        baseDeDatosMock.add(chofer);
    }

    @Override
    public Optional<Chofer> buscarPorLegajo(String legajo) {
        if (legajo == null) return Optional.empty();
        return baseDeDatosMock.stream()
                .filter(c -> c.getLegajo().equalsIgnoreCase(legajo.trim()))
                .findFirst();
    }

    @Override
    public List<Chofer> buscarTodos() {
        return new ArrayList<>(baseDeDatosMock);
    }
}
