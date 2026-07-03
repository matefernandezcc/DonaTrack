package com.donatrack.incentivos.domain.entities.misiones;
import java.util.UUID;
import com.donatrack.incentivos.domain.entities.Insignia;
import com.donatrack.incentivos.domain.entities.PerfilDonante;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Mision {
    private UUID id = UUID.randomUUID();
    private String nombre;
    private Insignia recompensa;
    private TipoMetricaMision tipoMetrica;
    private int objetivo;

    public Mision(String nombre, Insignia recompensa, TipoMetricaMision tipoMetrica, int objetivo) {
        this.nombre = nombre;
        this.recompensa = recompensa;
        this.tipoMetrica = tipoMetrica;
        this.objetivo = objetivo;
    }

    public boolean evaluar(PerfilDonante perfil) {
        return extraerValor(perfil) >= objetivo;
    }

    public String getProgresoActual(PerfilDonante perfil) {
        return extraerValor(perfil) + " / " + objetivo + " " + getUnidadMetrica();
    }

    private int extraerValor(PerfilDonante perfil) {
        if (perfil.getMetricas() == null) {
            return 0;
        }
        
        switch (this.tipoMetrica) {
            case DONACIONES_EXITOSAS:
                return perfil.getMetricas().totalDonacionesExitosas();
            case MAX_BIENES:
                return perfil.getMetricas().maxBienesPorDonacion();
            case MESES_CONSECUTIVOS:
                return perfil.getMetricas().rachaDeMeses();
            case CATEGORIAS_DISTINTAS:
                return perfil.getMetricas().categoriasDonadas() != null ? 
                       perfil.getMetricas().categoriasDonadas().size() : 0;
            default:
                return 0;
        }
    }

    private String getUnidadMetrica() {
        switch (this.tipoMetrica) {
            case DONACIONES_EXITOSAS:
                return "donaciones exitosas";
            case MAX_BIENES:
                return "max bienes por donación";
            case MESES_CONSECUTIVOS:
                return "meses consecutivos";
            case CATEGORIAS_DISTINTAS:
                return "categorías distintas";
            default:
                return "";
        }
    }
}
