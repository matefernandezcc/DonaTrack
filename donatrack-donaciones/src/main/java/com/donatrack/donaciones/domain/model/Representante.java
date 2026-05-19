package com.donatrack.donaciones.domain.model;

public class Representante extends Rol {
    private String cargo;
    private PersonaJuridica Organizacion;

    public Representante(String cargo, PersonaJuridica organizacion) {
        this.cargo = cargo;
        this.Organizacion = organizacion;
    }

    // Getters y Setters
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public PersonaJuridica getOrganizacion() { return Organizacion; }
    public void setOrganizacion(PersonaJuridica organizacion) { this.Organizacion = organizacion; }
}
