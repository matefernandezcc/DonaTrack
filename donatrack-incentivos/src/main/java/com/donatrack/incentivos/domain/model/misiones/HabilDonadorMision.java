package com.donatrack.incentivos.domain.model.misiones;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.PerfilDonante;

public class HabilDonadorMision extends Mision {

    private int cantidadBienesObjetivo;

    public HabilDonadorMision(int cantidadBienesObjetivo, Insignia recompensa) {
        super("Hábil Donador: más de " + cantidadBienesObjetivo + " bienes en una donación", recompensa);
        this.cantidadBienesObjetivo = cantidadBienesObjetivo;
    }

    @Override
    public boolean evaluar(PerfilDonante perfil) {
        return perfil.getMaxBienesEnUnaDonacion() >= cantidadBienesObjetivo;
    }

    @Override
    public String getProgresoActual(PerfilDonante perfil) {
        return perfil.getMaxBienesEnUnaDonacion() + " / " + cantidadBienesObjetivo + " max bienes en una donación";
    }
}
