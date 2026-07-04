package com.donatrack.donaciones.domain.entities.necesidades;

import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Necesidad {
  private UUID id;
  private String descripcion;
  private LocalDate fechaSolicitud;
  private Subcategoria subcategoriaRequerida;

  protected Necesidad(String descripcion, Subcategoria subcategoriaRequerida) {
    this.id = UUID.randomUUID();
    this.descripcion = descripcion;
    this.subcategoriaRequerida = subcategoriaRequerida;
    this.fechaSolicitud = LocalDate.now();
  }
}
