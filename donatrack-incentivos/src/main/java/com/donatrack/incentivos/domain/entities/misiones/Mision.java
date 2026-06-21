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
        return extraerValor(perfil) + " / " + objetivo + " " + tipoMetrica.getUnidad();
    }

    private int extraerValor(PerfilDonante perfil) {
        return tipoMetrica.extraerValor(perfil);
    }
}
