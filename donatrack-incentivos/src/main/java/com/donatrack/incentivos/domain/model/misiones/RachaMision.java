package com.donatrack.incentivos.domain.model.misiones;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.PerfilDonante;

public class RachaMision extends Mision {

    private int mesesObjetivo;

    public RachaMision(int mesesObjetivo, Insignia recompensa) {
        super("Racha " + mesesObjetivo + " meses", recompensa);
        this.mesesObjetivo = mesesObjetivo;
    }

    @Override
    public boolean evaluar(PerfilDonante perfil) {
        return perfil.getMesesConsecutivosDonando() >= mesesObjetivo;
    }

    @Override
    public String getProgresoActual(PerfilDonante perfil) {
        return perfil.getMesesConsecutivosDonando() + " / " + mesesObjetivo + " meses";
    }
}
