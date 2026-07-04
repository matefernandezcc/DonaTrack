package com.donatrack.donaciones.domain.entities.necesidades;

import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadRecurrente extends Necesidad {
  private double cantidadObjetivo;
  private Boolean activa;
  private List<PeriodoNecesidad> historialPeriodos;
  private PeriodoNecesidad periodoActual;
  private TipoPeriodo tipoPeriodo;

  public NecesidadRecurrente(
      String descripcion,
      Subcategoria subcategoriaRequerida,
      double cantidadObjetivo,
      TipoPeriodo tipoPeriodo) {
    super(descripcion, subcategoriaRequerida);
    this.cantidadObjetivo = cantidadObjetivo;
    this.tipoPeriodo = tipoPeriodo;
    this.activa = true;
    this.historialPeriodos = new ArrayList<>();
    
    // Inicializar el período actual
    LocalDate inicio = LocalDate.now();
    LocalDate fin = inicio.plusDays(tipoPeriodo.getDias() - 1);
    this.periodoActual = new PeriodoNecesidad(inicio, fin);
  }

  public PeriodoNecesidad cerrarPeriodoYCrearSiguiente() {
    if (!this.activa) {
      return null;
    }
    this.historialPeriodos.add(this.periodoActual);
    
    LocalDate nuevoInicio = this.periodoActual.getFechaFin().plusDays(1);
    LocalDate nuevoFin = nuevoInicio.plusDays(this.tipoPeriodo.getDias() - 1);
    this.periodoActual = new PeriodoNecesidad(nuevoInicio, nuevoFin);
    return this.periodoActual;
  }

  public void darDeBaja() {
    this.activa = false;
  }
}
