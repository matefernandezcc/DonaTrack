package com.donatrack.incentivos.domain.model.misiones;
import java.util.UUID;
import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.PerfilDonante;

public abstract class Mision {
    private UUID id = UUID.randomUUID();
    private String nombre;
    private Insignia recompensa;

    protected Mision(String nombre, Insignia recompensa) {
        this.nombre = nombre;
        this.recompensa = recompensa;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Insignia getRecompensa() {
        return recompensa;
    }

    /**
     * @param perfil El perfil del donante a evaluar
     * @return true si la misión ha sido cumplida, false de lo contrario
     */
    public abstract boolean evaluar(PerfilDonante perfil);

    /**
     * @param perfil El perfil del donante
     * @return porcentaje de progreso o una descripción textual del progreso
     */
    public abstract String getProgresoActual(PerfilDonante perfil);
}
