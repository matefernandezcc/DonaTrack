package com.donatrack.donaciones.domain.model;
import java.util.List;

public interface EstrategiaSegmentacion {
    // Recibe la lista completa de bienes y devuelve la lista de donaciones ya segmentadas
    List<Donacion> segmentar(List<Bien> bienes);
}
