package com.donatrack.donaciones.domain.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Persona {
    private UUID id;
    private String email;
    private Contacto contacto;
    private List<Rol> roles;

    // Constructor
    public Persona(String email, Contacto contacto) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.contacto = contacto;
        this.roles = new ArrayList<>(); 
    }

    public void agregarRol(Rol r) {
        this.roles.add(r);
    }

    public void actualizarInformacion(Map<String, Object> datosNuevos) {
        // La persona sabe cómo actualizarse a sí misma respetando el encapsulamiento.
        // Aquí extraemos los datos del mapa y pisamos los atributos correspondientes.
        if (datosNuevos.containsKey("email")) {
            this.email = (String) datosNuevos.get("email");
        }
        if (datosNuevos.containsKey("contacto")) {
            this.contacto = (Contacto) datosNuevos.get("contacto");
        }
        // Las clases hijas (Humana o Jurídica) podrán sobrescribir este método
        // para agregar la actualización de sus atributos propios (nombre, razón social, etc.).
    }

    public UUID getId() {return id;}
    public void setId(UUID id) {this.id = id;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public Contacto getContacto() {return contacto;}
    public void setContacto(Contacto contacto) {this.contacto = contacto;}

    public List<Rol> getRoles() {return roles;}
    public void setRoles(List<Rol> roles) {this.roles = roles;}
}