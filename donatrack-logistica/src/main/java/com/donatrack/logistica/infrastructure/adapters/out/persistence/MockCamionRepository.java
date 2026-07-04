package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.CamionRepositoryPort;
import com.donatrack.logistica.domain.entities.reparto.Camion;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MockCamionRepository implements CamionRepositoryPort {

    private final List<Camion> baseDeDatosMock = new ArrayList<>();

    public MockCamionRepository() {
        // Cargar camiones por defecto para pruebas y planificación
        baseDeDatosMock.add(new Camion("AA123BB", 10.0, 2.5, 1000.0));
        baseDeDatosMock.add(new Camion("CC789DD", 20.0, 3.2, 2500.0));
    }

    @Override
    public void guardar(Camion camion) {
        baseDeDatosMock.removeIf(c -> c.getPatente().equalsIgnoreCase(camion.getPatente()));
        baseDeDatosMock.add(camion);
    }

    @Override
    public Optional<Camion> buscarPorPatente(String patente) {
        return baseDeDatosMock.stream()
                .filter(c -> c.getPatente().equalsIgnoreCase(patente))
                .findFirst();
    }

    @Override
    public List<Camion> obtenerTodos() {
        return new ArrayList<>(baseDeDatosMock);
    }

    @Override
    public void eliminar(String patente) {
        baseDeDatosMock.removeIf(c -> c.getPatente().equalsIgnoreCase(patente));
    }
}
