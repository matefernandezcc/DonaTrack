package com.donatrack.donaciones.domain.model;
import java.util.ArrayList;
import java.util.List;
import com.donatrack.donaciones.domain.enums.EstadoDonacion;
// import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class Donante extends Rol {
    @Getter @Setter private List<Donacion> donacionesRealizadas;
    @Getter private List<RecepcionDonacion> historialRecepciones;


    public Donante() {
        super();
        this.donacionesRealizadas = new ArrayList<>();
        this.historialRecepciones = new ArrayList<>();
    }

    public void agregarDonacion(Donacion d) {this.donacionesRealizadas.add(d);}
    public void agregarRecepcion(RecepcionDonacion r) {this.historialRecepciones.add(r);}

    // --- Métodos de comportamiento ---

    public List<EstadoDonacion> consultarEstadoTodasLasDonaciones(List<Donacion> donacionesRealizadas) {
        return donacionesRealizadas.stream().map(Donacion::getEstado).toList();}

    public EstadoDonacion consultarEstadoDonacion(Donacion d) {return d.getEstado();}

    public void filtrarDonaciones(EstadoDonacion estado, String categoria) {
        // Lógica para filtrar el historial de donaciones realizadas por este usuario
    }

    public void verUbicacionCamion() {
        // Lógica para consultar la ubicación en tiempo real del camión asignado
    }
}
