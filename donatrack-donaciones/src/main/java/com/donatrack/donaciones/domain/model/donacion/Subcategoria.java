package com.donatrack.donaciones.domain.model.donacion;

import lombok.Getter;
import lombok.Setter;

public class Subcategoria {
  @Getter @Setter private String nombre;
  @Getter @Setter private String descripcion;
  @Getter @Setter private Categoria categoriaPadre;

  public Subcategoria(String nombre, String descripcion) {
    this.nombre = nombre;
    this.descripcion = descripcion;
  }

  public boolean esSubcategoriaDe(Categoria otraCategoria) {
    return this.categoriaPadre == otraCategoria;
  }
}
