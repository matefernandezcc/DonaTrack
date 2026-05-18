package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.EstadoDonacion;
import java.util.ArrayList;
import java.util.List;

public class Donante extends Rol {
    private List<Donacion> donacionesRealizadas;

    public Donante() {
        super();
        this.donacionesRealizadas = new ArrayList<>();
    }

    public void agregarDonacion(Donacion d) {this.donacionesRealizadas.add(d);}

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
}
