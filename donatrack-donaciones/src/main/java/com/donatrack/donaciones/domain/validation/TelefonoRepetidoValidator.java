package com.donatrack.donaciones.domain.validation;

import com.donatrack.donaciones.domain.model.Persona;

public class TelefonoRepetidoValidator extends PersonaValidator {

    // En un caso real, aquí se inyectaría un repositorio (ej. PersonaRepository)
    // para verificar si el teléfono ya está registrado en la base de datos.

    @Override
    public boolean validar(Persona persona) {
        if (persona.getContacto() != null && persona.getContacto().getTelefono() != null) {
          
        }

        return chequearSiguiente(persona);
    }
}
