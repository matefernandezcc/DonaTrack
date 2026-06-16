package com.donatrack.donaciones.domain.model.necesidades;

import com.donatrack.donaciones.domain.model.donacion.Bien;
import com.donatrack.donaciones.domain.model.donacion.Categoria;
import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.enums.EstadoNecesidad;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class NecesidadExtraordinaria extends Necesidad {
  @Getter @Setter private double cantidadRequerida;
  @Getter private List<Donacion> donacionesParciales;

  public NecesidadExtraordinaria(
      String descripcion, Categoria subcategoriaRequerida, double cantidadRequerida) {
    super(descripcion, subcategoriaRequerida);
    this.cantidadRequerida = cantidadRequerida;
    this.donacionesParciales = new ArrayList<>();
  }

  public void acumularDonacionesParciales(Donacion nuevaDonacion) {
    this.donacionesParciales.add(nuevaDonacion);
    double totalAcumulado = 0;

    for (Donacion d : this.donacionesParciales) {
      for (Bien b : d.getBienes()) {
        totalAcumulado += b.getCantidad();
      }
    }

    if (totalAcumulado >= this.cantidadRequerida) {
      this.setEstado(EstadoNecesidad.CUBIERTA);
    }
  }

  public double cantidadAcumulada() {
    return this.donacionesParciales.stream()
        .flatMap(donacion -> donacion.getBienes().stream())
        .mapToDouble(Bien::getCantidad)
        .sum();
  }

  public double cantidadPendiente() {
    return this.cantidadRequerida - this.cantidadAcumulada();
  }
}
