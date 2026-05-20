package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.EstadoDonacion;
import java.time.LocalDate;

public class HistorialEstado {
    private EstadoDonacion estado;
    private LocalDate fecha;
    private String observacion;
    private Administrador usuario; // El administrador que registró el cambio

    public HistorialEstado(EstadoDonacion estado, String observacion, Administrador usuario) {
        this.estado = estado;
        this.fecha = LocalDate.now(); // Se setea el día de hoy automáticamente
        this.observacion = observacion;
        this.usuario = usuario;
    }

    // Getters y Setters
    public EstadoDonacion getEstado() { return estado; }
    public void setEstado(EstadoDonacion estado) { this.estado = estado; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public Administrador getUsuario() { return usuario; }
    public void setUsuario(Administrador usuario) { this.usuario = usuario; }
}
