package com.donatrack.incentivos.domain.model.misiones;
import lombok.Getter;

@Getter
public enum TipoMetricaMision {
    DONACIONES_EXITOSAS("donaciones exitosas"),
    MAX_BIENES("max bienes en una donación"),
    MESES_CONSECUTIVOS("meses consecutivos"),
    CATEGORIAS_DISTINTAS("categorías distintas");

    private final String unidad;

    TipoMetricaMision(String unidad) {
        this.unidad = unidad;
    }
}
