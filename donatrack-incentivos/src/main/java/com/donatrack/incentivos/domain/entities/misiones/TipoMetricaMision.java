package com.donatrack.incentivos.domain.entities.misiones;

import lombok.Getter;
import com.donatrack.incentivos.domain.entities.PerfilDonante;

@Getter
public enum TipoMetricaMision {
    DONACIONES_EXITOSAS("donaciones exitosas") {
        @Override
        public int extraerValor(PerfilDonante perfil) {
            return perfil.getMetricas() != null ? perfil.getMetricas().totalDonacionesExitosas() : 0;
        }
    },
    MAX_BIENES("max bienes por donación") {
        @Override
        public int extraerValor(PerfilDonante perfil) {
            return perfil.getMetricas() != null ? perfil.getMetricas().maxBienesPorDonacion() : 0;
        }
    },
    MESES_CONSECUTIVOS("meses consecutivos") {
        @Override
        public int extraerValor(PerfilDonante perfil) {
            return perfil.getMetricas() != null ? perfil.getMetricas().rachaDeMeses() : 0;
        }
    },
    CATEGORIAS_DISTINTAS("categorías distintas") {
        @Override
        public int extraerValor(PerfilDonante perfil) {
            return (perfil.getMetricas() != null && perfil.getMetricas().categoriasDonadas() != null)
                    ? perfil.getMetricas().categoriasDonadas().size()
                    : 0;
        }
    };

    private final String unidad;

    TipoMetricaMision(String unidad) {
        this.unidad = unidad;
    }

    public abstract int extraerValor(PerfilDonante perfil);
}
