package com.donatrack.donaciones.domain.model;
import java.time.LocalDate;

public abstract class Rol {
    private LocalDate fechaAlta;

    public Rol() {
        this.fechaAlta = LocalDate.now(); 
    }

    public Rol(LocalDate fechaAlta) {this.fechaAlta = fechaAlta;}

    // Getters y Setters
    public LocalDate getFechaAlta() {return fechaAlta;}
    public void setFechaAlta(LocalDate fechaAlta) {this.fechaAlta = fechaAlta;}
}