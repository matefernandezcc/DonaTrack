package com.donatrack.incentivos.domain.model.categoria;

import com.donatrack.incentivos.domain.model.PerfilDonante;

public class ColaboradorState extends CategoriaDonanteState {

    public ColaboradorState(PerfilDonante perfil) {
        super(perfil);
    }

    @Override
    public void avanzarCategoria() {
        perfil.setCategoria(new SostenedorState(perfil));
        perfil.cargarMisionesDeCategoriaActual();
    }

    @Override
    public CategoriaDonanteEnum getValorEnum() {
        return CategoriaDonanteEnum.COLABORADOR;
    }
}
