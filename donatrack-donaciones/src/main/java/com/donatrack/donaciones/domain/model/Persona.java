package com.donatrack.donaciones.domain.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Persona {
    private UUID id;
    private String email;
    private Contacto contacto;
     private Direccion direccion;
    private List<Rol> roles;

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

    // Getters y Setters
    public UUID getId() {return id;}
    public void setId(UUID id) {this.id = id;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public Contacto getContacto() {return contacto;}
    public void setContacto(Contacto contacto) {this.contacto = contacto;}

    public List<Rol> getRoles() {return roles;}
    public void setRoles(List<Rol> roles) {this.roles = roles;}

    public Direccion getDireccion() {return direccion;}
    public void setDireccion(Direccion direccion) {this.direccion = direccion;}
}