package com.donatrack.donaciones.domain.model.donacion.segmentador;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.donatrack.donaciones.domain.model.donacion.Bien;

public class SegmentarPorEstado implements EstrategiaSegmentacion {

  @Override
  public List<List<Bien>> segmentar(List<List<Bien>> listasDeBienes) {

    List<List<Bien>> listasSegmentadas = new ArrayList<>();

    for (List<Bien> listaAIterar : listasDeBienes) {
      // Se toma el primer bien como muestra
      Bien muestra = listaAIterar.get(0);

      if (muestra.getEsUsado() == null) {
        // Caso 1: No es relevante el estado
        // Pasa intacto
        listasSegmentadas.add(listaAIterar);
      } else {
        // Caso 2: El estado es relevante
        // Se subdivide en 2: true "usados" y los false "no usados"
        Map<Boolean, List<Bien>> bienesPorEstado =
            listaAIterar.stream().collect(Collectors.groupingBy(Bien::getEsUsado));

        // Agrega las subdivisiones finales al resultado
        listasSegmentadas.addAll(bienesPorEstado.values());
      }
    }

    return listasSegmentadas;
  }
}
