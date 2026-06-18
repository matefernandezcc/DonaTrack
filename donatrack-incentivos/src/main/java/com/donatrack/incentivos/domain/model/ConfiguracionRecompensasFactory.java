package com.donatrack.incentivos.domain.model;

import com.donatrack.incentivos.domain.model.categoria.CategoriaDonanteEnum;
import com.donatrack.incentivos.domain.model.misiones.MisionesPorNivel;
import com.donatrack.incentivos.domain.model.misiones.MisionesPorNivelColaborador;
import com.donatrack.incentivos.domain.model.misiones.MisionesPorNivelSostenedor;
import com.donatrack.incentivos.domain.model.misiones.MisionesPorNivelTransformador;

import java.util.HashMap;
import java.util.Map;

public class ConfiguracionRecompensasFactory {

    private static final Map<CategoriaDonanteEnum, MisionesPorNivel> PROTOTIPOS = new HashMap<>();

    static {
        PROTOTIPOS.put(CategoriaDonanteEnum.COLABORADOR, new MisionesPorNivelColaborador());
        PROTOTIPOS.put(CategoriaDonanteEnum.SOSTENEDOR, new MisionesPorNivelSostenedor());
        PROTOTIPOS.put(CategoriaDonanteEnum.TRANSFORMADOR, new MisionesPorNivelTransformador());
    }

    public static MisionesPorNivel obtenerMisionesPara(PerfilDonante perfil, CategoriaDonanteEnum categoria) {
        MisionesPorNivel prototipo = PROTOTIPOS.get(categoria);
        if (prototipo == null) {
            return null;
        }
        MisionesPorNivel clon = prototipo.clonar();
        clon.setPerfilAsociado(perfil);
        return clon;
    }
}
