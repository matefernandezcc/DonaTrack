package com.donatrack.incentivos.domain.model.misiones;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.categoria.CategoriaDonanteEnum;
import java.util.ArrayList;
import java.util.List;

public class MisionesPorNivelColaborador extends MisionesPorNivel {

    public MisionesPorNivelColaborador() {
        this.categoriaObtener = CategoriaDonanteEnum.COLABORADOR;
        this.misiones.add(new DonacionesExitosasMision(2, new Insignia("Buen Inicio", "Lograste tus primeras 2 donaciones.")));
        this.misiones.add(new RachaMision(2, new Insignia("Donante Frecuente", "Donaste 2 meses seguidos.")));
        this.misionActual = this.misiones.isEmpty() ? null : this.misiones.remove(0);
    }

    @Override
    public MisionesPorNivel clonar() {
        MisionesPorNivelColaborador clone = new MisionesPorNivelColaborador();
        clone.setPerfilAsociado(this.percorAsociado());
        clone.setFechaAsignacion(this.fechaAsignacion);
        List<Mision> currentMisiones = new ArrayList<>(this.misiones);
        clone.setMisiones(currentMisiones);
        clone.setMisionActual(this.misionActual);
        return clone;
    }

    private com.donatrack.incentivos.domain.model.PerfilDonante percorAsociado() {
        return this.perfilAsociado;
    }
}
