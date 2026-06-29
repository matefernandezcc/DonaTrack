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
    MAX_BIENES("max bienes en una donación") {
        @Override
        public int extraerValor(PerfilDonante perfil) {
            return perfil.getMetricas() != null ? perfil.getMetricas().maxBienesEnUnaDonacion() : 0;
        }
    },
    MESES_CONSECUTIVOS("meses consecutivos") {
        @Override
        public int extraerValor(PerfilDonante perfil) {
            return perfil.getMetricas() != null ? perfil.getMetricas().mesesConsecutivosDonando() : 0;
        }
    },
    CATEGORIAS_DISTINTAS("categorías distintas") {
        @Override
        public int extraerValor(PerfilDonante perfil) {
            return (perfil.getMetricas() != null && perfil.getMetricas().categoriasUnicasDonadas() != null)
                    ? perfil.getMetricas().categoriasUnicasDonadas().size()
                    : 0;
        }
    };

    private final String unidad;

    TipoMetricaMision(String unidad) {
        this.unidad = unidad;
    }

    public abstract int extraerValor(PerfilDonante perfil);
}
