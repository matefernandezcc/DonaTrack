package com.donatrack.donaciones.domain.model;
import java.time.LocalDate;
import java.util.UUID;

import com.donatrack.donaciones.domain.enums.EstadoNecesidad;

public abstract class Necesidad {
    private String descripcion;
    private EstadoNecesidad estado;
    private Categoria subcategoriaRequerida;
    private UUID id;
    private LocalDate fechaSolicitud;

    public Necesidad(String descripcion, Categoria subcategoriaRequerida) {
        this.descripcion = descripcion;
        this.subcategoriaRequerida = subcategoriaRequerida;
        this.estado = EstadoNecesidad.PENDIENTE;
        this.id = UUID.randomUUID();
        this.fechaSolicitud = LocalDate.now();
    }

    
    public boolean estaCubierta(){
     return this.estado == EstadoNecesidad.CUBIERTA;
    }

    // Getters y Setters
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public UUID getId() { return id; }

    public LocalDate getFechaSolicitud() { return fechaSolicitud; }

    public EstadoNecesidad getEstado() { return estado; }
    public void setEstado(EstadoNecesidad estado) { this.estado = estado; }

    public Categoria getSubcategoriaRequerida() { return subcategoriaRequerida; }
    public void setSubcategoriaRequerida(Categoria subcategoriaRequerida) { this.subcategoriaRequerida = subcategoriaRequerida; }

}