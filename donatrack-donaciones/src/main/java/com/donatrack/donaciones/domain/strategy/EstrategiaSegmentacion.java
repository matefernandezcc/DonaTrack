package com.donatrack.donaciones.domain.strategy;

import com.donatrack.donaciones.domain.model.Bien;
import java.util.List;

public interface EstrategiaSegmentacion {
  // Recibe la lista completa de bienes y devuelve la lista de donaciones ya segmentadas
  List<List<Bien>> segmentar(List<List<Bien>> bienes);
}
