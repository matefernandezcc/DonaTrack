package com.donatrack.donaciones.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CategoriaTest {
  Categoria categoriaPadre = new Categoria("ropa", "ropaGeneral");
  Categoria categoriaHija = new Categoria("camisas", "camisas");

  @Test
  public void testEsSubcategoriaDePadre() {
    Categoria categoriaNieta = new Categoria("camisasMangaCorta", "camisasMangaCorta");
    categoriaPadre.agregarSubcategoria(categoriaHija);

    // Debería fallar al agregar subcategoría a una hija
    assertEquals(false, categoriaHija.agregarSubcategoria(categoriaNieta));

    assertEquals(true, categoriaHija.esSubcategoriaDe(categoriaPadre));
    assertEquals(false, categoriaNieta.esSubcategoriaDe(categoriaPadre));
  }

  @Test
  public void testNoEssubcategoriaDe() {
    Categoria categoriaDistinta = new Categoria("calzado", "calzado");

    assertEquals(false, categoriaHija.esSubcategoriaDe(categoriaDistinta));
  }
}
