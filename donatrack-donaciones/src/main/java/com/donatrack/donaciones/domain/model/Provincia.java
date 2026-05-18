package com.donatrack.donaciones.domain.model;

public class Provincia {
    private String nombreProvincia;
    private Pais pais;

    public Provincia(String nombreProvincia, Pais pais) {
        this.nombreProvincia = nombreProvincia;
        this.pais = pais;
    }

    // Getters y Setters
    public String getNombreProvincia() { return nombreProvincia; }
    public void setNombreProvincia(String nombreProvincia) { this.nombreProvincia = nombreProvincia; }

    public Pais getPais() { return pais; }
    public void setPais(Pais pais) { this.pais = pais; }
}
