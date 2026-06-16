package com.donatrack.donaciones.domain.model.persona;

import com.donatrack.donaciones.domain.enums.MedioContacto;
import com.donatrack.donaciones.domain.enums.TipoDocumento;

public class PersonaFactory {

    public Persona crearDesdeCSV(String[] datos) {
        String tipoPersona = datos[0];
        String tipoDoc = datos[1];
        String documento = datos[2];
        String nombreRazonSocial = datos[3];
        String email = datos[4];
        String telefono = datos[5];

        TipoDocumento tipoDocu = TipoDocumento.valueOf(tipoDoc);
        Contacto contacto = new Contacto(email, telefono, null, MedioContacto.CORREO);

        if ("HUMANA".equals(tipoPersona)) {
            return new PersonaHumana(
                email,
                contacto,
                null, // Dirección no proporcionada en el CSV
                new DocumentoIdentidad(tipoDocu, documento),
                nombreRazonSocial,
                null, // Apellido se debe separar del nombre completo
                0 // Edad no proporcionada en el CSV
            );
        } else {
            return new PersonaJuridica(
                email,
                contacto,
                null, // Dirección no proporcionada en el CSV
                new DocumentoIdentidad(TipoDocumento.CUIT, documento),
                nombreRazonSocial,
                null, // Tipo de persona jurídica no proporcionado en el CSV
                null // Rubro no proporcionado en el CSV
            );
        }
    }
}
