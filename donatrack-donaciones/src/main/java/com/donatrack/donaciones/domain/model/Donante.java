package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.EstadoDonacion;

public class Donante extends Rol {

    public Donante() {
        super(); // Llama al constructor de Rol para setear la fecha de alta
    }

    // --- Métodos de comportamiento ---

    public void filtrarDonaciones(EstadoDonacion estado, String categoria) {
        // Lógica para filtrar el historial de donaciones realizadas por este usuario
    }

    public void verUbicacionCamion() {
        // Lógica para consultar la ubicación en tiempo real del camión asignado
    }
}
