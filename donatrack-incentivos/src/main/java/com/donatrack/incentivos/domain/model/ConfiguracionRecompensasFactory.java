package com.donatrack.incentivos.domain.model;

import com.donatrack.incentivos.domain.model.categoria.CategoriaDonanteEnum;
import com.donatrack.incentivos.domain.model.misiones.CompletitudMision;
import com.donatrack.incentivos.domain.model.misiones.DonacionesExitosasMision;
import com.donatrack.incentivos.domain.model.misiones.HabilDonadorMision;
import com.donatrack.incentivos.domain.model.misiones.Mision;
import com.donatrack.incentivos.domain.model.misiones.RachaMision;

import java.util.LinkedList;
import java.util.Queue;

public class ConfiguracionRecompensasFactory {

    public static Queue<Mision> obtenerMisionesPara(CategoriaDonanteEnum categoria) {
        Queue<Mision> misiones = new LinkedList<>();

        switch (categoria) {
            case COLABORADOR:
                misiones.offer(new DonacionesExitosasMision(2, new Insignia("Buen Inicio", "Lograste tus primeras 2 donaciones.")));
                misiones.offer(new RachaMision(2, new Insignia("Donante Frecuente", "Donaste 2 meses seguidos.")));
                break;
            case SOSTENEDOR:
                misiones.offer(new HabilDonadorMision(5, new Insignia("Manos Llenas", "Donaste más de 5 bienes en una sola vez.")));
                misiones.offer(new CompletitudMision(3, new Insignia("Multifacético", "Donaste en 3 categorías diferentes.")));
                misiones.offer(new RachaMision(4, new Insignia("Constancia", "Donaste 4 meses seguidos.")));
                break;
            case TRANSFORMADOR:
                misiones.offer(new DonacionesExitosasMision(10, new Insignia("Leyenda", "10 donaciones exitosas.")));
                misiones.offer(new CompletitudMision(5, new Insignia("Omnipresente", "Ayudaste en 5 categorías.")));
                break;
        }

        return misiones;
    }
}
