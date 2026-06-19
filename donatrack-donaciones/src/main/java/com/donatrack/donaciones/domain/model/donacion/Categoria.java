package com.donatrack.donaciones.domain.model.donacion;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class Categoria {
  @Getter @Setter private String nombre;
  @Getter @Setter private String descripcion;
  @Getter private List<Subcategoria> subcategorias;

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
