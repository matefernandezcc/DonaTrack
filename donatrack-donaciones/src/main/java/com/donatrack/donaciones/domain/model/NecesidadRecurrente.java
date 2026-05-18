package com.donatrack.donaciones.domain.model;

public class NecesidadRecurrente extends Necesidad {
    private double cantidadObjetivo;
    private String periodo;

    public NecesidadRecurrente(String descripcion, Categoria subcategoriaRequerida, double cantidadObjetivo, String periodo) {
        super(descripcion, subcategoriaRequerida);
        this.cantidadObjetivo = cantidadObjetivo;
        this.periodo = periodo;
    }

    // Getters y Setters
    public double getCantidadObjetivo() { return cantidadObjetivo; }
    public void setCantidadObjetivo(double cantidadObjetivo) { this.cantidadObjetivo = cantidadObjetivo; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
}