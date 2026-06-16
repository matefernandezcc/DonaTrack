package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.domain.model.persona.Persona;
import com.donatrack.donaciones.domain.repository.PersonaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MockPersonaRepository implements PersonaRepository {

    private final List<Persona> baseDeDatosMock = new ArrayList<>();

    @Override
    public List<Persona> obtenerTodas() {
        return new ArrayList<>(baseDeDatosMock);
    }

    @Override
    public void guardar(Persona persona) {
        baseDeDatosMock.removeIf(p -> p.getId().equals(persona.getId()));
        baseDeDatosMock.add(persona);
    }
}
