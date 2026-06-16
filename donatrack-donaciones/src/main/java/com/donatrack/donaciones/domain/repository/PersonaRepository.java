package com.donatrack.donaciones.domain.repository;

import com.donatrack.donaciones.domain.model.persona.Persona;
import java.util.List;

public interface PersonaRepository {
    List<Persona> obtenerTodas();
    void guardar(Persona persona);
}
