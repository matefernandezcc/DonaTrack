package com.donatrack.donaciones.domain.repository;

import com.donatrack.donaciones.domain.model.persona.Persona;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonaRepository {
    Optional<Persona> buscarPorId(UUID id);
    List<Persona> obtenerTodas();
    void guardar(Persona persona);
}
