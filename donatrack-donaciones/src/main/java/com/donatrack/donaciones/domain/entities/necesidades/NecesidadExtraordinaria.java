package com.donatrack.donaciones.domain.entities.necesidades;

import com.donatrack.donaciones.domain.entities.donacion.Bien;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.enums.EstadoNecesidad;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadExtraordinaria extends Necesidad {
  private double cantidadRequerida;
  private EstadoNecesidad estado;
  private List<Donacion> donacionesRecibidas;

  public NecesidadExtraordinaria(
      String descripcion, Subcategoria subcategoriaRequerida, double cantidadRequerida) {
    super(descripcion, subcategoriaRequerida);
    this.cantidadRequerida = cantidadRequerida;
    this.estado = EstadoNecesidad.ABIERTA;
    this.donacionesRecibidas = new ArrayList<>();
  }

  public void acumularDonacionesParciales(Donacion nuevaDonacion) {
    this.donacionesRecibidas.add(nuevaDonacion);
    nuevaDonacion.cambiarEstado(EstadoDonacion.ASIGNADA, "Asignada a necesidad extraordinaria", null);
    
    if (this.estaCubierta()) {
      this.estado = EstadoNecesidad.SATISFECHA;
    }
  }

  public Boolean estaCubierta() {
    return this.cantidadAcumulada() >= this.cantidadRequerida;
  }

  public double cantidadAcumulada() {
    return this.donacionesRecibidas.stream()
        .flatMap(donacion -> donacion.getBienes().stream())
        .mapToDouble(Bien::getCantidad)
        .sum();
  }

  public double cantidadPendiente() {
    return this.cantidadRequerida - this.cantidadAcumulada();
  }
}
