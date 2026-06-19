package com.donatrack.incentivos.domain.model.misiones;
import java.util.UUID;
import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.PerfilDonante;

import lombok.Getter;
import lombok.Setter;


public class Mision {
    @Getter private UUID id = UUID.randomUUID();
    @Getter @Setter private String nombre;
    @Getter @Setter private Insignia recompensa;
    @Getter @Setter private TipoMetricaMision tipoMetrica;
    @Getter @Setter private int objetivo;

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
        return extraerValor(perfil) + " / " + objetivo + " " + tipoMetrica.getUnidad();
    }

    private int extraerValor(PerfilDonante perfil) {
        return switch (tipoMetrica) {
            case DONACIONES_EXITOSAS -> perfil.getDonacionesExitosas();
            case MAX_BIENES -> perfil.getMaxBienesEnUnaDonacion();
            case MESES_CONSECUTIVOS -> perfil.getMesesConsecutivosDonando();
            case CATEGORIAS_DISTINTAS -> perfil.getCategoriasUnicasDonadas() != null ? perfil.getCategoriasUnicasDonadas().size() : 0;
        };
    }
}
