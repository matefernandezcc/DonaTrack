package com.donatrack.donaciones.domain.model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Bien {
    private String descripcion;
    private double cantidad;
    private String unidadMedicion;
    private Boolean esUsado;
    private LocalDate fechaVencimiento;
    private List<Foto> fotos;
    private Categoria subcategoria;

    public Bien(String descripcion, double cantidad, String unidadMedicion, boolean esUsado, LocalDate fechaVencimiento, String foto) {
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.unidadMedicion = unidadMedicion;
        this.esUsado = esUsado;
        this.fechaVencimiento = fechaVencimiento;
        this.fotos = new ArrayList<>();
    }

    public void agregarFoto(Foto foto) {this.fotos.add(foto);}

    // Getters y Setters
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public String getUnidadMedicion() { return unidadMedicion; }
    public void setUnidadMedicion(String unidadMedicion) { this.unidadMedicion = unidadMedicion; }

    public Boolean isEsUsado() { return esUsado; }
    public void setEsUsado(boolean esUsado) { this.esUsado = esUsado; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public Categoria getSubcategoria() { return subcategoria; }
    public void setSubcategoria(Categoria subcategoria) { this.subcategoria = subcategoria; }

    public List<Foto> getFotos() { return fotos; }
    public void setFotos(List<Foto> fotos) { this.fotos = fotos; }
}