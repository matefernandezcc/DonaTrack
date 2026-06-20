package com.donatrack.incentivos.domain.model.misiones;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.categoria.CategoriaDonante;
import java.util.ArrayList;
import java.util.List;

public class MisionesPorNivelColaborador extends MisionesPorNivel {

    public MisionesPorNivelColaborador() {
        this.categoriaObtener = CategoriaDonante.COLABORADOR;
        this.misiones.add(new Mision("Lograr 2 donaciones exitosas",
                new Insignia("Buen Inicio", "Lograste tus primeras 2 donaciones."),
                TipoMetricaMision.DONACIONES_EXITOSAS, 2));
        this.misiones.add(new Mision("Racha 2 meses", new Insignia("Donante Frecuente", "Donaste 2 meses seguidos."),
                TipoMetricaMision.MESES_CONSECUTIVOS, 2));
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
