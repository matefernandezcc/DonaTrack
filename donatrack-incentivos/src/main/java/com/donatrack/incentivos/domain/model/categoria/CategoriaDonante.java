package com.donatrack.incentivos.domain.model.categoria;

public enum CategoriaDonante {
    COLABORADOR {
        @Override
        public CategoriaDonante siguienteNivel() {
            return SOSTENEDOR;
        }
    },
    SOSTENEDOR {
        @Override
        public CategoriaDonante siguienteNivel() {
            return TRANSFORMADOR;
        }
    },
    TRANSFORMADOR {
        @Override
        public CategoriaDonante siguienteNivel() {
            return TRANSFORMADOR; // Max level
        }
    };

    public abstract CategoriaDonante siguienteNivel();
}
