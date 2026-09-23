package com.donatrack.donaciones.domain.entities.persona;

import com.donatrack.donaciones.domain.entities.enums.MedioContacto;
import com.donatrack.donaciones.domain.entities.enums.TipoDocumento;

public class PersonaFactory {

    public Persona crearDesdeCSV(String[] datos) {
        String tipoPersona = datos[0] != null ? datos[0].replaceAll("^\\uFEFF", "").trim() : "";
        String tipoDoc = datos.length > 1 ? datos[1].trim() : "DNI";
        String documento = datos.length > 2 ? datos[2].trim() : "";
        String nombreRazonSocial = datos.length > 3 ? datos[3].trim() : "";
        String email = datos.length > 4 ? datos[4].trim() : "";
        String telefono = datos.length > 5 ? datos[5].trim() : null;

        TipoDocumento tipoDocu = TipoDocumento.valueOf(tipoDoc);
        Contacto contacto = new Contacto(email, telefono, null, MedioContacto.CORREO);

        if ("HUMANA".equals(tipoPersona)) {
            String nombre = nombreRazonSocial;
            String apellido = "";
            if (nombreRazonSocial != null && nombreRazonSocial.contains(" ")) {
                String[] partes = nombreRazonSocial.trim().split(" ", 2);
                nombre = partes[0];
                apellido = partes[1];
            }
            return new PersonaHumana(
                email,
                contacto,
                null,
                new DocumentoIdentidad(tipoDocu, documento),
                nombre,
                apellido,
                0
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
