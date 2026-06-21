package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.PersonaRepository;

import com.donatrack.donaciones.domain.entities.persona.Persona;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public Optional<Persona> buscarPorId(UUID id) {
        return baseDeDatosMock.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Persona> buscarPorEmail(String email) {
        if (email == null) return Optional.empty();
        return baseDeDatosMock.stream()
                .filter(p -> p.getEmail() != null && p.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst();
    }
}
