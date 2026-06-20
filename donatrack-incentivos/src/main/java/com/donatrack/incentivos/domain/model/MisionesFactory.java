package com.donatrack.incentivos.domain.model;

import com.donatrack.incentivos.domain.model.categoria.CategoriaDonante;
import java.util.LinkedList;
import java.util.Queue;
import com.donatrack.incentivos.domain.model.misiones.Mision;
import com.donatrack.incentivos.domain.model.misiones.TipoMetricaMision;

public class MisionesFactory {

    public static Queue<Mision> crearMisionesPara(CategoriaDonante categoria) {
        Queue<Mision> misiones = new LinkedList<>();
        switch (categoria) {
            case COLABORADOR:
                misiones.add(new Mision("Lograr 2 donaciones exitosas",
                        new Insignia("Buen Inicio", "Lograste tus primeras 2 donaciones."),
                        TipoMetricaMision.DONACIONES_EXITOSAS, 2));
                misiones.add(new Mision("Racha 2 meses", new Insignia("Donante Frecuente", "Donaste 2 meses seguidos."),
                        TipoMetricaMision.MESES_CONSECUTIVOS, 2));
                break;
            case SOSTENEDOR:
                misiones.add(new Mision("Hábil Donador: más de 5 bienes en una donación",
                        new Insignia("Manos Llenas", "Donaste más de 5 bienes en una sola vez."), TipoMetricaMision.MAX_BIENES,
                        5));
                misiones.add(new Mision("Completitud 3 categorías",
                        new Insignia("Multifacético", "Donaste en 3 categorías diferentes."),
                        TipoMetricaMision.CATEGORIAS_DISTINTAS, 3));
                misiones.add(new Mision("Racha 4 meses", new Insignia("Constancia", "Donaste 4 meses seguidos."),
                        TipoMetricaMision.MESES_CONSECUTIVOS, 4));
                break;
            case TRANSFORMADOR:
                misiones.add(new Mision("Lograr 10 donaciones exitosas",
                        new Insignia("Leyenda", "10 donaciones exitosas."), TipoMetricaMision.DONACIONES_EXITOSAS, 10));
                misiones.add(new Mision("Completitud 5 categorías",
                        new Insignia("Omnipresente", "Ayudaste en 5 categorías."), TipoMetricaMision.CATEGORIAS_DISTINTAS, 5));
                break;
        }
        return misiones;
    }
}
