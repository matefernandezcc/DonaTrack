package com.donatrack.donaciones.domain.model.donacion.segmentador;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.donatrack.donaciones.domain.model.donacion.Bien;
import com.donatrack.donaciones.domain.model.donacion.Subcategoria;

public class SegmentarPorSubcategoria implements EstrategiaSegmentacion {

  @Override
  public List<List<Bien>> segmentar(List<List<Bien>> listasDeBienes) {

    List<List<Bien>> listasSegmentadas = new ArrayList<>();

    // Se itera sobre las listas que entraron en la tuberia
    for (List<Bien> listaAIterar : listasDeBienes) {

      // Robo la idea de Maxi: Agrupo bienes usando su subcategoria como clave 
      // Te da un mapa temporal { "Ropa invierno" -> [campera, bufanda], "Muebles" -> [sillas] }
      Map<Subcategoria, List<Bien>> bienesEnLista =
          listaAIterar.stream().collect(Collectors.groupingBy(Bien::getSubcategoria));

      listasSegmentadas.addAll(bienesEnLista.values());
    }

    return listasSegmentadas;
  }
}
