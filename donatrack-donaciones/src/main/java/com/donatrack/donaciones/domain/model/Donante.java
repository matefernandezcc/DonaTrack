package com.donatrack.donaciones.domain.model;
import java.util.ArrayList;
import java.util.List;

import com.donatrack.donaciones.domain.enums.EstadoDonacion;

public class Donante extends Rol {
    private List<Donacion> donacionesRealizadas;
    private List<RecepcionDonacion> historialRecepciones;

    public Donante() {
        super();
        this.donacionesRealizadas = new ArrayList<>();
        this.historialRecepciones = new ArrayList<>();
    }

    public void agregarDonacion(Donacion d) {this.donacionesRealizadas.add(d);}
    public void agregarRecepcion(RecepcionDonacion r) {this.historialRecepciones.add(r);}

    // --- Métodos de comportamiento ---
    public void filtrarDonaciones(EstadoDonacion estado, String categoria) {
        // Lógica para filtrar el historial de donaciones realizadas por este usuario
    }

    public void verUbicacionCamion() {
        // Lógica para consultar la ubicación en tiempo real del camión asignado
    }

    // Getters y Setters
    public List<Donacion> getDonaciones() { return donacionesRealizadas; }
    public void setDonaciones(List<Donacion> donacionesRealizadas) { this.donacionesRealizadas = donacionesRealizadas; }
    
    public List<RecepcionDonacion> getHistorialRecepciones() { return historialRecepciones; }
}
