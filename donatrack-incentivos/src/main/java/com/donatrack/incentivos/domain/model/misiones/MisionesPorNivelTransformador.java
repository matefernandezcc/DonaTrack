package com.donatrack.incentivos.domain.model.misiones;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.categoria.CategoriaDonanteEnum;
import java.util.ArrayList;
import java.util.List;

public class MisionesPorNivelTransformador extends MisionesPorNivel {

    public MisionesPorNivelTransformador() {
        this.categoriaObtener = CategoriaDonanteEnum.TRANSFORMADOR;
        this.misiones.add(new Mision("Lograr 10 donaciones exitosas", new Insignia("Leyenda", "10 donaciones exitosas."), TipoMetricaMision.DONACIONES_EXITOSAS, 10));
        this.misiones.add(new Mision("Completitud 5 categorías", new Insignia("Omnipresente", "Ayudaste en 5 categorías."), TipoMetricaMision.CATEGORIAS_DISTINTAS, 5));
        this.misionActual = this.misiones.isEmpty() ? null : this.misiones.remove(0);
    }

    @Override
    public MisionesPorNivel clonar() {
        MisionesPorNivelTransformador clone = new MisionesPorNivelTransformador();
        clone.setPerfilAsociado(this.perfilAsociado);
        clone.setFechaAsignacion(this.fechaAsignacion);
        List<Mision> currentMisiones = new ArrayList<>(this.misiones);
        clone.setMisiones(currentMisiones);
        clone.setMisionActual(this.misionActual);
        return clone;
    }
}
