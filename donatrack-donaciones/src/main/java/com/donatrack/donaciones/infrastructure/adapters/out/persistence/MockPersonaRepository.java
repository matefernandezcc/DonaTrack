package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.persona.PersonaHumana;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.DocumentoIdentidad;
import com.donatrack.donaciones.domain.entities.enums.MedioContacto;
import com.donatrack.donaciones.domain.entities.enums.TipoDocumento;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import com.donatrack.donaciones.domain.entities.roles.Administrador;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MockPersonaRepository implements PersonaRepository {

    private final List<Persona> baseDeDatosMock = new ArrayList<>();

    public MockPersonaRepository() {
        // Inicializar Donante de prueba
        PersonaHumana donante = new PersonaHumana(
            "donante@test.com",
            new Contacto("donante@test.com", "123456789", "123456789", MedioContacto.CORREO),
            null,
            new DocumentoIdentidad(TipoDocumento.DNI, "12345678"),
            "Dario", "Dardo", 30
        );
        donante.setId(UUID.fromString("d3b07384-d113-4ec6-a51d-402f06c1af0b"));
        donante.agregarRol(new Donante());
        baseDeDatosMock.add(donante);

        // Inicializar Administrador de prueba
        PersonaHumana admin = new PersonaHumana(
            "admin@test.com",
            new Contacto("admin@test.com", "987654321", "987654321", MedioContacto.CORREO),
            null,
            new DocumentoIdentidad(TipoDocumento.DNI, "87654321"),
            "Pedro", "Perez", 45
        );
        admin.setId(UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"));
        admin.agregarRol(new Administrador(UUID.randomUUID()));
        baseDeDatosMock.add(admin);
    }

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

    @Override
    public Optional<Persona> buscarPorRolId(UUID rolId) {
        return baseDeDatosMock.stream()
                .filter(p -> p.getRoles() != null && p.getRoles().stream().anyMatch(r -> r.getId().equals(rolId)))
                .findFirst();
    }
}
