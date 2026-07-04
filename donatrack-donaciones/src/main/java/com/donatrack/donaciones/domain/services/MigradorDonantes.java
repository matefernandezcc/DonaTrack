package com.donatrack.donaciones.domain.services;

import com.donatrack.donaciones.domain.entities.donacion.Archivo;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.importador.ImportadorStrategy;
import com.donatrack.donaciones.application.ports.out.PersonaRepository;

import java.util.List;

public class MigradorDonantes {

    private final ImportadorStrategy importadorStrategy;
    private final PersonaRepository personaRepository;

    public MigradorDonantes(ImportadorStrategy importadorStrategy, PersonaRepository personaRepository) {
        this.importadorStrategy = importadorStrategy;
        this.personaRepository = personaRepository;
    }

    public List<Persona> procesarMigracion(Archivo archivo) {
        importadorStrategy.importar(archivo);
        return personaRepository.obtenerTodas();
    }
}
