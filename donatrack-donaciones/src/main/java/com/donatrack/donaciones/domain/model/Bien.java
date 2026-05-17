package com.donatrack.donaciones.domain.model;
import java.time.LocalDate;

public class Bien {
    private String descripcion;
    private double cantidad;
    private String unidadMedicion;
    private boolean esUsado;
    private LocalDate fechaVencimiento;
    private String foto; // Ruta o URL de la foto
    private Categoria subcategoria;

    public Bien(String descripcion, double cantidad, String unidadMedicion, boolean esUsado, LocalDate fechaVencimiento, String foto) {
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.unidadMedicion = unidadMedicion;
        this.esUsado = esUsado;
        this.fechaVencimiento = fechaVencimiento;
        this.foto = foto;
    }

    // Getters y Setters
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public String getUnidadMedicion() { return unidadMedicion; }
    public void setUnidadMedicion(String unidadMedicion) { this.unidadMedicion = unidadMedicion; }

    public boolean isEsUsado() { return esUsado; }
    public void setEsUsado(boolean esUsado) { this.esUsado = esUsado; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public Categoria getSubcategoria() { return subcategoria; }
    public void setSubcategoria(Categoria subcategoria) { this.subcategoria = subcategoria; }
}