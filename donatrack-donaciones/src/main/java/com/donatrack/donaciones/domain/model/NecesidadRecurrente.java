package com.donatrack.donaciones.domain.model;

import com.donatrack.donaciones.domain.enums.EstadoNecesidad;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

public class NecesidadRecurrente extends Necesidad {
  @Getter @Setter private double cantidadObjetivo;
  @Getter @Setter private double periodo;

  public NecesidadRecurrente(
      String descripcion,
      Categoria subcategoriaRequerida,
      double cantidadObjetivo,
      double periodo) {
    super(descripcion, subcategoriaRequerida);
    this.cantidadObjetivo = cantidadObjetivo;
    this.periodo = periodo;
  }

  public LocalDate FechaVencimientoPeriodo() {
    return this.getFechaSolicitud().plusDays((long) this.periodo);
  }

  public void cumplirObjetivo(Donacion nuevaDonacion) {
    if (this.cantidadAcumulada(nuevaDonacion) >= this.cantidadObjetivo) {
      this.setEstado(EstadoNecesidad.CUBIERTA);
    }
  }

  public double cantidadAcumulada(Donacion nuevaDonacion) {
    return nuevaDonacion.getBienes().stream().mapToDouble(Bien::getCantidad).sum();
  }
}
