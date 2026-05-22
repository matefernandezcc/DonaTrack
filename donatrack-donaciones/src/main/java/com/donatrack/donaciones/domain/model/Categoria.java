package com.donatrack.donaciones.domain.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class Categoria {
  @Getter @Setter private String nombre;
  @Getter @Setter private String descripcion;
  @Getter private List<Categoria> subcategorias;

  public Categoria(String nombre, String descripcion) {
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.subcategorias = new ArrayList<>();
  }

  public void agregarSubcategoria(Categoria categoria) {
    this.subcategorias.add(categoria);
  }

  public boolean esSubcategoriaDe(Categoria otraCategoria) {
    // Caso base: si mi categoría padre es "otraCategoria"
    if (otraCategoria.getSubcategorias().contains(this)) {
      return true;
    }
    // Búsqueda recursiva: revisamos si estoy en algún nivel más profundo del árbol
    for (Categoria sub : otraCategoria.getSubcategorias()) {
      if (this.esSubcategoriaDe(sub)) {
        return true;
      }
    }
    return false;
  }
}
