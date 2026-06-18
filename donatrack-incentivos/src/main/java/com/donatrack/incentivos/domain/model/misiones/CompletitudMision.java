package com.donatrack.incentivos.domain.model.misiones;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.PerfilDonante;

public class CompletitudMision extends Mision {

    private int categoriasObjetivo;

    public CompletitudMision(int categoriasObjetivo, Insignia recompensa) {
        super("Completitud " + categoriasObjetivo + " categorías", recompensa);
        this.categoriasObjetivo = categoriasObjetivo;
    }

    @Override
    public boolean evaluar(PerfilDonante perfil) {
        return perfil.getCategoriasUnicasDonadas() != null && 
               perfil.getCategoriasUnicasDonadas().size() >= categoriasObjetivo;
    }

    @Override
    public String getProgresoActual(PerfilDonante perfil) {
        int actual = perfil.getCategoriasUnicasDonadas() != null ? perfil.getCategoriasUnicasDonadas().size() : 0;
        return actual + " / " + categoriasObjetivo + " categorías distintas";
    }
}
