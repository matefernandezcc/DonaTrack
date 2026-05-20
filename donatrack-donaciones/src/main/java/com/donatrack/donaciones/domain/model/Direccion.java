package com.donatrack.donaciones.domain.model;

public class Direccion {
    private String calle;
    private double altura;
    private String localidad;
    private Provincia provincia; 
    private Pais pais;
    private String codigoPostal;
    private Coordenada coordenadas;

    public Direccion(String calle, double altura, String localidad, Provincia provincia, Pais pais, String codigoPostal, Coordenada coordenadas) {
        this.calle = calle;
        this.altura = altura;
        this.localidad = localidad;
        this.provincia = provincia;
        this.pais = pais;
        this.codigoPostal = codigoPostal;
        this.coordenadas = coordenadas;
    }

    // Getters y Setters
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public double getAltura() { return altura; }
    public void setAltura(double altura) { this.altura = altura; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public Provincia getProvincia() { return provincia; }
    public void setProvincia(Provincia provincia) { this.provincia = provincia; }

    public Pais getPais() { return pais; }
    public void setPais(Pais pais) { this.pais = pais; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public Coordenada getCoordenadas() { return coordenadas; }
    public void setCoordenadas(Coordenada coordenadas) { this.coordenadas = coordenadas; }
}