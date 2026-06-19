package com.donatrack.incentivos.domain.model.misiones;

import com.donatrack.incentivos.domain.model.PerfilDonante;
import com.donatrack.incentivos.domain.model.categoria.CategoriaDonanteEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class MisionesPorNivel {
    protected List<Mision> misiones = new ArrayList<>();
    protected Mision misionActual;
    protected PerfilDonante perfilAsociado;
    protected CategoriaDonanteEnum categoriaObtener;
    protected LocalDate fechaAsignacion = LocalDate.now();

    public abstract MisionesPorNivel clonar();

    public Mision avanzarMision() {
        if (misiones.isEmpty()) {
            misionActual = null;
        } else {
            misionActual = misiones.remove(0);
        }
        return misionActual;
    }
}
