package com.donatrack.donaciones.domain.validation;

import com.donatrack.donaciones.domain.model.persona.Persona;

public abstract class PersonaValidator {
    private PersonaValidator siguienteValidador;

    public PersonaValidator enlazarCon(PersonaValidator siguiente) {
        this.siguienteValidador = siguiente;
        return siguiente;
    }

    public abstract boolean validar(Persona persona);

    protected boolean chequearSiguiente(Persona persona) {
        if (siguienteValidador != null) {
            return siguienteValidador.validar(persona);
        }
        return true;
    }
}
