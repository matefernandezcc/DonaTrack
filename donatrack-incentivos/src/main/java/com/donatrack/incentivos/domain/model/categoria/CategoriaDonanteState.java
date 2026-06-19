package com.donatrack.incentivos.domain.model.categoria;

import com.donatrack.incentivos.domain.model.PerfilDonante;

public abstract class CategoriaDonanteState {
    
    protected PerfilDonante perfil;

    protected CategoriaDonanteState(PerfilDonante perfil) {
        this.perfil = perfil;
    }

    public abstract void avanzarCategoria();

    public abstract CategoriaDonanteEnum getValorEnum();
}
