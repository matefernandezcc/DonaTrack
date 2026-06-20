package com.donatrack.donaciones.domain.model.donacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Bien {
  private String descripcion;
  private double cantidad;
  private String unidadMedicion;
  private Boolean esUsado;
  private LocalDate fechaVencimiento;
  private List<Foto> fotos;
  private Subcategoria subcategoria;

  public Bien(
      String descripcion,
      double cantidad,
      String unidadMedicion,
      boolean esUsado,
      LocalDate fechaVencimiento) {
    this.descripcion = descripcion;
    this.cantidad = cantidad;
    this.unidadMedicion = unidadMedicion;
    this.esUsado = esUsado;
    this.fechaVencimiento = fechaVencimiento;
    this.fotos = new ArrayList<>();
  }

  public void agregarFoto(Foto foto) {
    this.fotos.add(foto);
  }
}
