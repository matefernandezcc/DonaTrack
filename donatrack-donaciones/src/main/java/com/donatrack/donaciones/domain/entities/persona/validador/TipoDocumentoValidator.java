package com.donatrack.donaciones.domain.entities.persona.validador;

import com.donatrack.donaciones.domain.entities.enums.TipoDocumento;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.persona.PersonaHumana;
import com.donatrack.donaciones.domain.entities.persona.PersonaJuridica;

public class TipoDocumentoValidator extends PersonaValidator {

    @Override
    public boolean validar(Persona persona) {
        if (persona.getDocumento() == null) {
            // Error: El documento de identidad no puede ser nulo.
            return false;
        }

        if (persona instanceof PersonaHumana) {
            if (persona.getDocumento().getTipo() == TipoDocumento.CUIT) {
                // Error: Una persona humana debe tener un documento de tipo DNI o Pasaporte.
                return false;
            }
        } else if (persona instanceof PersonaJuridica || (persona.getDocumento().getTipo() != TipoDocumento.CUIT)) {
            // Error: Una persona jurídica debe tener un documento de tipo CUIT.
            return false;
        }

        return chequearSiguiente(persona);
    }
}
