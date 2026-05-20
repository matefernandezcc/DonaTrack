package com.donatrack.donaciones.domain.model;
import java.util.List;

public interface AsignacionStrategy {
    // Firma del método que recibe donaciones y necesidades, y recomienda cómo emparejarlas
    List<Donacion> recomendarNecesidades(List<Donacion> donaciones, List<Necesidad> necesidades);
}