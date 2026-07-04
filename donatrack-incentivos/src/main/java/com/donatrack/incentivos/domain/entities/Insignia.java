package com.donatrack.incentivos.domain.entities;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class Insignia {
    private UUID id;
    private String nombre;
    private String descripcion;
    private String urlImagen;
    private LocalDate fechaObtencion;
    private boolean visiblePublicamente;

    public Insignia(String nombre, String descripcion) {
        this.id = UUID.randomUUID();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaObtencion = LocalDate.now();
        this.visiblePublicamente = true;
    }
}
