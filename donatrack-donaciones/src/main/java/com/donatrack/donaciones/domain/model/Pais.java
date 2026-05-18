package com.donatrack.donaciones.domain.model;

public class Pais {
    private String nombrePais;
    private String nacionalidad;

    public Pais(String nombrePais, String nacionalidad) {
        this.nombrePais = nombrePais;
        this.nacionalidad = nacionalidad;
    }

    // Getters y Setters
    public String getNombrePais() { return nombrePais; }
    public void setNombrePais(String nombrePais) { this.nombrePais = nombrePais; }

    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }
}