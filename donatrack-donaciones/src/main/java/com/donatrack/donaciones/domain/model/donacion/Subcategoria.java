package com.donatrack.donaciones.domain.model.donacion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Subcategoria {
  private String nombre;
  private String descripcion;
  private Categoria categoriaPadre;

  public Subcategoria(String nombre, String descripcion) {
    this.nombre = nombre;
    this.descripcion = descripcion;
  }

  public boolean esSubcategoriaDe(Categoria otraCategoria) {
    return this.categoriaPadre == otraCategoria;
  }
}
