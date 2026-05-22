package com.donatrack.donaciones.domain.model;

import lombok.Getter;
import lombok.Setter;

public class Foto {
    @Getter @Setter private String descripcion;
    @Getter @Setter private String url;

    public Foto(String descripcion, String url) {
        this.descripcion = descripcion;
        this.url = url;
    }
}