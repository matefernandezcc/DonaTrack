package com.donatrack.donaciones.application.ports.out;

import com.donatrack.donaciones.domain.entities.persona.Persona;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonaRepository {
    Optional<Persona> buscarPorId(UUID id);
    Optional<Persona> buscarPorEmail(String email);
    List<Persona> obtenerTodas();
    void guardar(Persona persona);
    Optional<Persona> buscarPorRolId(UUID rolId);
}
