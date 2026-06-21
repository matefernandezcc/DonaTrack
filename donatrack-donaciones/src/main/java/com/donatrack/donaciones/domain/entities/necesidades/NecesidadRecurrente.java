package com.donatrack.donaciones.domain.entities.necesidades;

import com.donatrack.donaciones.domain.entities.donacion.Bien;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.enums.EstadoNecesidad;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadRecurrente extends Necesidad {
  private double cantidadObjetivo;
  private double periodo;

  public NecesidadRecurrente(
      String descripcion,
      Subcategoria subcategoriaRequerida,
      double cantidadObjetivo,
      double periodo) {
    super(descripcion, subcategoriaRequerida);
    this.cantidadObjetivo = cantidadObjetivo;
    this.periodo = periodo;
  }

  public LocalDate fechaVencimientoPeriodo() {
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
