package com.donatrack.incentivos.domain.model.categoria;

import com.donatrack.incentivos.domain.model.PerfilDonante;

public class SostenedorState extends CategoriaDonanteState {

    public SostenedorState(PerfilDonante perfil) {
        super(perfil);
    }

    @Override
    public void avanzarCategoria() {
        perfil.setCategoria(new TransformadorState(perfil));
        perfil.cargarMisionesDeCategoriaActual();
    }

    @Override
    public CategoriaDonanteEnum getValorEnum() {
        return CategoriaDonanteEnum.SOSTENEDOR;
    }
}
