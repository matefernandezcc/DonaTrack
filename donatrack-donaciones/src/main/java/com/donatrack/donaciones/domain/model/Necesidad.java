package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.EstadoNecesidad;

public abstract class Necesidad {
    private String descripcion;
    private EstadoNecesidad estado;
    private Categoria subcategoriaRequerida;

    public Necesidad(String descripcion, Categoria subcategoriaRequerida) {
        this.descripcion = descripcion;
        this.subcategoriaRequerida = subcategoriaRequerida;
        this.estado = EstadoNecesidad.ABIERTA;
    }

    // Getters y Setters
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public EstadoNecesidad getEstado() { return estado; }
    public void setEstado(EstadoNecesidad estado) { this.estado = estado; }

    public Categoria getSubcategoriaRequerida() { return subcategoriaRequerida; }
    public void setSubcategoriaRequerida(Categoria subcategoriaRequerida) { this.subcategoriaRequerida = subcategoriaRequerida; }
}