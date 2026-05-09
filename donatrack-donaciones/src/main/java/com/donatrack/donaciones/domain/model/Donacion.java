package com.donatrack.donaciones.domain.model;

import java.util.List;

public class Donacion {
    private Necesidad necesidad;
    private EstadoDonacion estado;
    private List<Bien> bienes;
    private List<String> foto;
    private List<HistorialEstado> estados;
    private Administrador registradoPor;
    private Deposito deposito;
}