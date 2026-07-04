package com.donatrack.donaciones.domain.entities.necesidades;

public enum TipoPeriodo {
    SEMANAL(7),
    MENSUAL(30);

    private final int dias;

    TipoPeriodo(int dias) {
        this.dias = dias;
    }

    public int getDias() {
        return dias;
    }
}
