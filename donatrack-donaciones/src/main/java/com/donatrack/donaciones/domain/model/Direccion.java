package com.donatrack.donaciones.domain.model;

import lombok.Getter;
import lombok.Setter;

public class Direccion {
    @Getter @Setter private String calle;
    @Getter @Setter private double altura;
    @Getter @Setter private String localidad;
    @Getter @Setter private Provincia provincia; 
    @Getter @Setter private Pais pais;
    @Getter @Setter private String codigoPostal;
    @Getter @Setter private Coordenada coordenadas;

    public Direccion(String calle, double altura, String localidad, Provincia provincia, Pais pais, String codigoPostal, Coordenada coordenadas) {
        this.calle = calle;
        this.altura = altura;
        this.localidad = localidad;
        this.provincia = provincia;
        this.pais = pais;
        this.codigoPostal = codigoPostal;
        this.coordenadas = coordenadas;
    }
}