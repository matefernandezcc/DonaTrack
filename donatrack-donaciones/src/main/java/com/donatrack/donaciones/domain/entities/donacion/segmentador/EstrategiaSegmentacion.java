package com.donatrack.donaciones.domain.entities.donacion.segmentador;

import com.donatrack.donaciones.domain.entities.donacion.Bien;
import java.util.List;

public interface EstrategiaSegmentacion {
  // Recibe la lista completa de bienes y devuelve la lista de donaciones ya segmentadas
  List<List<Bien>> segmentar(List<List<Bien>> bienes);
}
