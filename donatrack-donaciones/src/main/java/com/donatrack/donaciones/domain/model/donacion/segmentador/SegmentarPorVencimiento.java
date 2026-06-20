package com.donatrack.donaciones.domain.model.donacion.segmentador;

import com.donatrack.donaciones.domain.model.donacion.Bien;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SegmentarPorVencimiento implements EstrategiaSegmentacion {

  @Override
  public List<List<Bien>> segmentar(List<List<Bien>> listasDeBienes) {

    List<List<Bien>> listasSegmentadas = new ArrayList<>();

    for (List<Bien> listaAIterar : listasDeBienes) {
      // Cada sublista tiene una subcategoria, se toma el primer bien como muestra
      Bien muestra = listaAIterar.get(0);

      if (muestra.getFechaVencimiento() == null) {
        // Caso 1: El bien no es perecedero. Pasa intacto a SegmentarPorEstado
        listasSegmentadas.add(listaAIterar);
      } else {
        // Caso 2: Es perecedero. (Ej: leche)
        // Aca el problema es que si es "no perecedero" como arroz blanco, deberia ir null en
        // fechaVencimiento
        // Se subdivide por fecha exacta
        Map<LocalDate, List<Bien>> bienesPorFecha =
            listaAIterar.stream().collect(Collectors.groupingBy(Bien::getFechaVencimiento));

        // Se agrega las nuevas subdivisiones al resultado final
        listasSegmentadas.addAll(bienesPorFecha.values());
      }
    }

    return listasSegmentadas;
  }
}
