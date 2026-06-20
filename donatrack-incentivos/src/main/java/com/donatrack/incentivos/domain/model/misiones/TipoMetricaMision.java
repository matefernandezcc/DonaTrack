package com.donatrack.incentivos.domain.model.misiones;
import lombok.Getter;
import com.donatrack.incentivos.domain.model.PerfilDonante;

@Getter
public enum TipoMetricaMision {
    DONACIONES_EXITOSAS("donaciones exitosas") {
        @Override
        public int extraerValor(PerfilDonante perfil) {
            return perfil.getMetricas() != null ? perfil.getMetricas().getDonacionesExitosas() : 0;
        }
    },
    MAX_BIENES("max bienes en una donación") {
        @Override
        public int extraerValor(PerfilDonante perfil) {
            return perfil.getMetricas() != null ? perfil.getMetricas().getMaxBienesEnUnaDonacion() : 0;
        }
    },
    MESES_CONSECUTIVOS("meses consecutivos") {
        @Override
        public int extraerValor(PerfilDonante perfil) {
            return perfil.getMetricas() != null ? perfil.getMetricas().getMesesConsecutivosDonando() : 0;
        }
    },
    CATEGORIAS_DISTINTAS("categorías distintas") {
        @Override
        public int extraerValor(PerfilDonante perfil) {
            return (perfil.getMetricas() != null && perfil.getMetricas().getCategoriasUnicasDonadas() != null) 
                    ? perfil.getMetricas().getCategoriasUnicasDonadas().size() : 0;
        }
    };

    private final String unidad;

    TipoMetricaMision(String unidad) {
        this.unidad = unidad;
    }

    public abstract int extraerValor(PerfilDonante perfil);
}
