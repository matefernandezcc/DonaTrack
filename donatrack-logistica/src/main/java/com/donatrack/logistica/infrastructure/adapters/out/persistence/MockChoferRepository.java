package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.ChoferRepositoryPort;
import com.donatrack.logistica.domain.entities.reparto.Chofer;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MockChoferRepository implements ChoferRepositoryPort {

    private final List<Chofer> baseDeDatosMock = new ArrayList<>();

    public MockChoferRepository() {
        // Cargar choferes por defecto para pruebas
        baseDeDatosMock.add(new Chofer("CH-001", "Juan Perez"));
        baseDeDatosMock.add(new Chofer("CH-002", "Maria Gomez"));
    }

    @Override
    public void guardar(Chofer chofer) {
        baseDeDatosMock.removeIf(c -> c.getLegajo().equalsIgnoreCase(chofer.getLegajo()));
        baseDeDatosMock.add(chofer);
    }

    @Override
    public Optional<Chofer> buscarPorLegajo(String legajo) {
        return baseDeDatosMock.stream()
                .filter(c -> c.getLegajo().equalsIgnoreCase(legajo))
                .findFirst();
    }

    @Override
    public List<Chofer> obtenerTodos() {
        return new ArrayList<>(baseDeDatosMock);
    }

    @Override
    public void eliminar(String legajo) {
        baseDeDatosMock.removeIf(c -> c.getLegajo().equalsIgnoreCase(legajo));
    }
}
