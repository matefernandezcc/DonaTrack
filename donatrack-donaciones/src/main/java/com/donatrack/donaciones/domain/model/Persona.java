package com.donatrack.donaciones.domain.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

public abstract class Persona {
    @Getter @Setter private UUID id;
    @Getter @Setter private String email;
    @Getter @Setter private Contacto contacto;
    @Getter @Setter private Direccion direccion;
    @Getter @Setter private List<Rol> roles;

    public Persona(String email, Contacto contacto, Direccion direccion) {
        this.id = UUID.randomUUID(); 
        this.email = email;
        this.contacto = contacto;
        this.direccion = direccion; 
        this.roles = new ArrayList<>(); 
    }

    public void agregarRol(Rol r) {
        this.validarRol(r);
        this.roles.add(r);
    }

    protected abstract void validarRol(Rol r);

    public void actualizarInformacion(Map<String, Object> datosNuevos) {
        // La persona sabe cómo actualizarse a sí misma respetando el encapsulamiento.

        if (datosNuevos.containsKey("email")) {
            this.email = (String) datosNuevos.get("email");
        }
        if (datosNuevos.containsKey("contacto")) {
            this.contacto = (Contacto) datosNuevos.get("contacto");
        }
        if (datosNuevos.containsKey("direccion")) {
            this.direccion = (Direccion) datosNuevos.get("direccion");
        }
        // Las clases hijas (Humana o Jurídica) podrán sobrescribir este método
    }
}