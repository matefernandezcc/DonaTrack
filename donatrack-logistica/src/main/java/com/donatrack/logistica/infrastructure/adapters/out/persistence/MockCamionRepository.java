package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.CamionRepository;
import com.donatrack.logistica.domain.entities.Camion;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MockCamionRepository implements CamionRepository {

    private final List<Camion> baseDeDatosMock = new ArrayList<>();

    public MockCamionRepository() {
        // Inicializar con algunos camiones mock
        baseDeDatosMock.add(new Camion("AAA-123", 20.0, 3.5, 5000.0));
        baseDeDatosMock.add(new Camion("BBB-456", 15.0, 3.0, 3500.0));
        baseDeDatosMock.add(new Camion("CCC-789", 30.0, 4.0, 8000.0));
    }

    @Override
    public void guardar(Camion camion) {
        baseDeDatosMock.removeIf(c -> c.getPatente().equalsIgnoreCase(camion.getPatente()));
        baseDeDatosMock.add(camion);
    }

    @Override
    public Optional<Camion> buscarPorPatente(String patente) {
        if (patente == null) return Optional.empty();
        return baseDeDatosMock.stream()
                .filter(c -> c.getPatente().equalsIgnoreCase(patente.trim()))
                .findFirst();
    }

    @Override
    public List<Camion> buscarTodos() {
        return new ArrayList<>(baseDeDatosMock);
    }
}
