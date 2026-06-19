package com.donatrack.incentivos.domain.model.misiones;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.categoria.CategoriaDonanteEnum;
import java.util.ArrayList;
import java.util.List;

public class MisionesPorNivelTransformador extends MisionesPorNivel {

    public MisionesPorNivelTransformador() {
        this.categoriaObtener = CategoriaDonanteEnum.TRANSFORMADOR;
        this.misiones.add(new DonacionesExitosasMision(10, new Insignia("Leyenda", "10 donaciones exitosas.")));
        this.misiones.add(new CompletitudMision(5, new Insignia("Omnipresente", "Ayudaste en 5 categorías.")));
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
