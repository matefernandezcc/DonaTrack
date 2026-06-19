package com.donatrack.incentivos.domain.model.misiones;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.categoria.CategoriaDonanteEnum;
import java.util.ArrayList;
import java.util.List;

public class MisionesPorNivelSostenedor extends MisionesPorNivel {

    public MisionesPorNivelSostenedor() {
        this.categoriaObtener = CategoriaDonanteEnum.SOSTENEDOR;
        this.misiones.add(new Mision("Hábil Donador: más de 5 bienes en una donación", new Insignia("Manos Llenas", "Donaste más de 5 bienes en una sola vez."), TipoMetricaMision.MAX_BIENES, 5));
        this.misiones.add(new Mision("Completitud 3 categorías", new Insignia("Multifacético", "Donaste en 3 categorías diferentes."), TipoMetricaMision.CATEGORIAS_DISTINTAS, 3));
        this.misiones.add(new Mision("Racha 4 meses", new Insignia("Constancia", "Donaste 4 meses seguidos."), TipoMetricaMision.MESES_CONSECUTIVOS, 4));
        this.misionActual = this.misiones.isEmpty() ? null : this.misiones.remove(0);
    }

    @Override
    public MisionesPorNivel clonar() {
        MisionesPorNivelSostenedor clone = new MisionesPorNivelSostenedor();
        clone.setPerfilAsociado(this.perfilAsociado);
        clone.setFechaAsignacion(this.fechaAsignacion);
        List<Mision> currentMisiones = new ArrayList<>(this.misiones);
        clone.setMisiones(currentMisiones);
        clone.setMisionActual(this.misionActual);
        return clone;
    }
}
