package com.donatrack.incentivos.domain.model;

import com.donatrack.incentivos.domain.model.categoria.CategoriaDonante;
import com.donatrack.incentivos.domain.model.misiones.MisionesPorNivel;
import com.donatrack.incentivos.domain.model.misiones.MisionesPorNivelColaborador;
import com.donatrack.incentivos.domain.model.misiones.MisionesPorNivelSostenedor;
import com.donatrack.incentivos.domain.model.misiones.MisionesPorNivelTransformador;

import java.util.HashMap;
import java.util.Map;

public class MisionesFactory {

    private static final Map<CategoriaDonante, MisionesPorNivel> PROTOTIPOS = new HashMap<>();

    static {
        PROTOTIPOS.put(CategoriaDonante.COLABORADOR, new MisionesPorNivelColaborador());
        PROTOTIPOS.put(CategoriaDonante.SOSTENEDOR, new MisionesPorNivelSostenedor());
        PROTOTIPOS.put(CategoriaDonante.TRANSFORMADOR, new MisionesPorNivelTransformador());
    }

    public static MisionesPorNivel obtenerMisionesPara(PerfilDonante perfil, CategoriaDonante categoria) {
        MisionesPorNivel prototipo = PROTOTIPOS.get(categoria);
        if (prototipo == null) {
            return null;
        }
        MisionesPorNivel clon = prototipo.clonar();
        clon.setPerfilAsociado(perfil);
        return clon;
    }
}
