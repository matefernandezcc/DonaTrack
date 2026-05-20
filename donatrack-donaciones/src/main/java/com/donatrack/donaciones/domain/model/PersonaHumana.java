package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.TipoDocumento;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class PersonaHumana extends Persona {
    @Getter @Setter private String nombre;
    @Getter @Setter private String apellido;
    @Getter @Setter private TipoDocumento tipoDocumento;
    @Getter @Setter private String documento;
    @Getter @Setter private int edad;

    public PersonaHumana(String email, Contacto contacto, Direccion direccion, String nombre, String apellido, TipoDocumento tipoDocumento, String documento, int edad) {
        super(email, contacto, direccion);
        this.nombre = nombre;
        this.apellido = apellido;
        this.tipoDocumento = tipoDocumento;
        this.documento = documento;
        this.edad = edad;
    }

    @Override protected void validarRol(Rol r) {
        // Regla de Negocio: Tira excepción si el rol es Beneficiario
        if (r instanceof Beneficiario) {
            throw new IllegalArgumentException("Error: Una persona humana no puede ser una Entidad Beneficiaria.");
        }
    }


    @Override public void actualizarInformacion(Map<String, Object> datosNuevos) {
        super.actualizarInformacion(datosNuevos); 
        
        if (datosNuevos.containsKey("nombre")) {
            this.nombre = (String) datosNuevos.get("nombre");
        }
        if (datosNuevos.containsKey("apellido")) {
            this.apellido = (String) datosNuevos.get("apellido");
        }
        if (datosNuevos.containsKey("documento")) {
            this.documento = (String) datosNuevos.get("documento");
        }
        if (datosNuevos.containsKey("edad")) {
            this.edad = (Integer) datosNuevos.get("edad");
        }
    }
}
