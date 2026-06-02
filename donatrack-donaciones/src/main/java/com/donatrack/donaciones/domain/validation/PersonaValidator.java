package com.donatrack.donaciones.domain.validation;

import com.donatrack.donaciones.domain.model.Persona;

public abstract class PersonaValidator {
    private PersonaValidator siguienteValidador;

    public PersonaValidator enlazarCon(PersonaValidator siguiente) {
        this.siguienteValidador = siguiente;
        return siguiente;
    }

    public abstract void validar(Persona persona);

    protected void chequearSiguiente(Persona persona) {
        if (siguienteValidador != null) {
            siguienteValidador.validar(persona);
        }
    }
}
