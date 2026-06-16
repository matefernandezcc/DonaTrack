package com.donatrack.donaciones.domain.model.necesidades;

import com.donatrack.donaciones.domain.enums.EstadoNecesidad;
import com.donatrack.donaciones.domain.model.donacion.Categoria;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public abstract class Necesidad {
  @Getter @Setter private String descripcion;
  @Getter @Setter private EstadoNecesidad estado;
  @Getter @Setter private Categoria subcategoriaRequerida;
  @Getter private UUID id;
  @Getter private LocalDate fechaSolicitud;

  protected Necesidad(String descripcion, Categoria subcategoriaRequerida) {
    this.descripcion = descripcion;
    this.subcategoriaRequerida = subcategoriaRequerida;
    this.estado = EstadoNecesidad.PENDIENTE;
    this.id = UUID.randomUUID();
    this.fechaSolicitud = LocalDate.now();
  }

  public boolean estaCubierta() {
    return this.estado == EstadoNecesidad.CUBIERTA;
  }
}
