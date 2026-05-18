package com.donatrack.donaciones.domain.model;

public class Foto {
    private String descripcion;
    private String url;

    public Foto(String descripcion, String url) {
        this.descripcion = descripcion;
        this.url = url;
    }

    // Getters y Setters
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}