package com.donatrack.donaciones.domain.model.necesidades;

import com.donatrack.donaciones.domain.model.donacion.Subcategoria;
import com.donatrack.donaciones.domain.model.enums.EstadoNecesidad;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Necesidad {
  private String descripcion;
  private EstadoNecesidad estado;
  private Subcategoria subcategoriaRequerida;
  private UUID id;
  private LocalDate fechaSolicitud;

  protected Necesidad(String descripcion, Subcategoria subcategoriaRequerida) {
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
