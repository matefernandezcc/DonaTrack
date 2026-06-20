package com.donatrack.donaciones.domain.model.donacion;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Categoria {
  private String nombre;
  private String descripcion;
  private List<Subcategoria> subcategorias;

  public Categoria(String nombre, String descripcion) {
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.subcategorias = new ArrayList<>();
  }

  public void agregarSubcategoria(Subcategoria subcategoria) {
    subcategoria.setCategoriaPadre(this);
    this.subcategorias.add(subcategoria);
  }
}
