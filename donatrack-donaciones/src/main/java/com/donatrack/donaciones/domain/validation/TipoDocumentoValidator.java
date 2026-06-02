package com.donatrack.donaciones.domain.validation;

import com.donatrack.donaciones.domain.enums.TipoDocumento;
import com.donatrack.donaciones.domain.model.Persona;
import com.donatrack.donaciones.domain.model.PersonaHumana;
import com.donatrack.donaciones.domain.model.PersonaJuridica;

public class TipoDocumentoValidator extends PersonaValidator {

    @Override
    public void validar(Persona persona) {
        if (persona.getDocumento() == null) {
            throw new IllegalArgumentException("Error: El documento de identidad no puede ser nulo.");
        }

        if (persona instanceof PersonaHumana) {
            if (persona.getDocumento().getTipo() == TipoDocumento.CUIT) {
                throw new IllegalArgumentException(
                    "Error: Una persona humana debe tener un documento de tipo DNI o Pasaporte.");
            }
        } else if (persona instanceof PersonaJuridica) {
            if (persona.getDocumento().getTipo() != TipoDocumento.CUIT) {
                throw new IllegalArgumentException(
                    "Error: Una persona jurídica debe tener un documento de tipo CUIT.");
            }
        }

        chequearSiguiente(persona);
    }
}
