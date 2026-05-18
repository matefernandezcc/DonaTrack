package com.donatrack.donaciones.domain.model;
import java.time.LocalDate;

public class Camion {
    private String patente;
    private String ubicacionActual;
    private LocalDate ultimaActualizacion;

    public Camion(String patente, String ubicacionActual) {
        this.patente = patente;
        this.ubicacionActual = ubicacionActual;
        this.ultimaActualizacion = LocalDate.now(); 
    }

    // --- Getters y Setters ---
    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }

    public LocalDate getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(LocalDate ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }

    public String getUbicacionActual() { return ubicacionActual; }
    // Modificamos el setter para que actualice la fecha automáticamente
    public void setUbicacionActual(String ubicacionActual) { 
        this.ubicacionActual = ubicacionActual;
        this.ultimaActualizacion = LocalDate.now(); 
    }
}