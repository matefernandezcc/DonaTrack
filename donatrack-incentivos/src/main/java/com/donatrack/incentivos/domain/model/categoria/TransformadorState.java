package com.donatrack.incentivos.domain.model.categoria;

import com.donatrack.incentivos.domain.model.PerfilDonante;

public class TransformadorState extends CategoriaDonanteState {

    public TransformadorState(PerfilDonante perfil) {
        super(perfil);
    }

    @Override
    public void avanzarCategoria() {
        // Es la máxima categoría, no avanza más.
        // Podría lanzar una excepción o simplemente no hacer nada.
    }

    @Override
    public CategoriaDonanteEnum getValorEnum() {
        return CategoriaDonanteEnum.TRANSFORMADOR;
    }
}
