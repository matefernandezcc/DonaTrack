package com.donatrack.donaciones.domain.model.persona.validador;

import com.donatrack.donaciones.domain.model.persona.Persona;

public class DocumentoRepetidoValidator extends PersonaValidator {

    // En un caso real, aquí se inyectaría un repositorio (ej. PersonaRepository)
    // para verificar si el número de documento ya está registrado en la base de datos.

    @Override
    public boolean validar(Persona persona) {
        if (persona.getDocumento() != null && persona.getDocumento().getNumero() != null) {

        }

        return chequearSiguiente(persona);
    }
}
