package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.TipoDocumento;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class PersonaHumana extends Persona {
    @Getter @Setter private String nombre;
    @Getter @Setter private String apellido;
    @Getter @Setter private int edad;

    public PersonaHumana(String email, Contacto contacto, Direccion direccion, DocumentoIdentidad documento, 
                         String nombre, String apellido, int edad) {
        // Pasamos el documento al constructor padre
        super(email, contacto, direccion, documento); 
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
    }

    @Override protected void validarRol(Rol r) {
        if (r instanceof Beneficiario) {
            throw new IllegalArgumentException("Error: Una persona humana no puede ser una Entidad Beneficiaria.");
        }
    }

    @Override protected void validarDocumentacion(DocumentoIdentidad d) {
        if (d.getTipo() == TipoDocumento.CUIT) {
            throw new IllegalArgumentException("Error: Una persona humana debe tener un documento de tipo DNI o Pasaporte.");
        }
    }

    @Override public void actualizarInformacion(Map<String, Object> datosNuevos) {
        // Esto ya se encarga de delegar al padre y actualizar de forma segura el documento si viene en el mapa
        super.actualizarInformacion(datosNuevos);  
        
        if (datosNuevos.containsKey("nombre")) {
            this.nombre = (String) datosNuevos.get("nombre");
        }
        if (datosNuevos.containsKey("apellido")) {
            this.apellido = (String) datosNuevos.get("apellido");
        }
        if (datosNuevos.containsKey("edad")) {
            this.edad = (Integer) datosNuevos.get("edad");
        }
    }
}