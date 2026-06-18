package com.donatrack.donaciones.domain.model.donacion;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class Categoria {
  @Getter @Setter private String nombre;
  @Getter @Setter private String descripcion;
  @Getter private List<Categoria> subcategorias;
  @Getter private Categoria categoriaPadre;

  public Categoria(String nombre, String descripcion) {
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.subcategorias = new ArrayList<>();
    this.categoriaPadre = null;
  }

  public boolean agregarSubcategoria(Categoria categoria) {
    // Una subcategoría (que ya tiene padre) no puede tener sus propias subcategorías
    if (this.categoriaPadre != null) {
      return false;
    }
    
    // Una categoría que ya tiene hijas no puede convertirse en subcategoría
    if (!categoria.getSubcategorias().isEmpty()) {
      return false;
    }

    categoria.categoriaPadre = this;
    this.subcategorias.add(categoria);
    return true;
  }

  public boolean esSubcategoriaDe(Categoria otraCategoria) {
    // Al limitarse a 2 niveles (Padre -> Hijo), la recursividad ya no es necesaria.
    return this.categoriaPadre == otraCategoria;
  }
}
