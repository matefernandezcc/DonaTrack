package com.donatrack.donaciones.domain.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class Bien {
  @Getter @Setter private String descripcion;
  @Getter @Setter private double cantidad;
  @Getter @Setter private String unidadMedicion;
  @Getter @Setter private Boolean esUsado;
  @Getter @Setter private LocalDate fechaVencimiento;
  @Getter @Setter private List<Foto> fotos;
  @Getter @Setter private Categoria subcategoria;

  public Bien(
      String descripcion,
      double cantidad,
      String unidadMedicion,
      boolean esUsado,
      LocalDate fechaVencimiento,
      String foto) {
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
