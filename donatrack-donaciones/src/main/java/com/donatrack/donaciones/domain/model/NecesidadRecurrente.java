package com.donatrack.donaciones.domain.model;

import lombok.Getter;
import lombok.Setter;

public class NecesidadRecurrente extends Necesidad {
    @Getter @Setter private double cantidadObjetivo;
    @Getter @Setter private String periodo;

    public NecesidadRecurrente(String descripcion, Categoria subcategoriaRequerida, double cantidadObjetivo, String periodo) {
        super(descripcion, subcategoriaRequerida);
        this.cantidadObjetivo = cantidadObjetivo;
        this.periodo = periodo;
    }
}