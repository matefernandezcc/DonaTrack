package com.donatrack.incentivos.domain.model.misiones;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.PerfilDonante;

public class DonacionesExitosasMision extends Mision {

    private int donacionesObjetivo;

    public DonacionesExitosasMision(int donacionesObjetivo, Insignia recompensa) {
        super("Lograr " + donacionesObjetivo + " donaciones exitosas", recompensa);
        this.donacionesObjetivo = donacionesObjetivo;
    }

    @Override
    public boolean evaluar(PerfilDonante perfil) {
        return perfil.getDonacionesExitosas() >= donacionesObjetivo;
    }

    @Override
    public String getProgresoActual(PerfilDonante perfil) {
        return perfil.getDonacionesExitosas() + " / " + donacionesObjetivo + " donaciones exitosas";
    }
}
