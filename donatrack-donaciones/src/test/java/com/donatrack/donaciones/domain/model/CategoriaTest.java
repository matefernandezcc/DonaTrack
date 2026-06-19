package com.donatrack.donaciones.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.donatrack.donaciones.domain.model.donacion.Categoria;
import com.donatrack.donaciones.domain.model.donacion.Subcategoria;

public class CategoriaTest {
  Categoria categoriaPadre = new Categoria("ropa", "ropaGeneral");
  Subcategoria categoriaHija = new Subcategoria("camisas", "camisas");

  @Test
  public void testEsSubcategoriaDePadre() {
    Subcategoria categoriaNieta = new Subcategoria("camisasMangaCorta", "camisasMangaCorta");
    categoriaPadre.agregarSubcategoria(categoriaHija);

    assertEquals(true, categoriaHija.esSubcategoriaDe(categoriaPadre));
    assertEquals(false, categoriaNieta.esSubcategoriaDe(categoriaPadre));
  }

  @Test
  public void testNoEssubcategoriaDe() {
    Categoria categoriaDistinta = new Categoria("calzado", "calzado");

    assertEquals(false, categoriaHija.esSubcategoriaDe(categoriaDistinta));
  }
}
