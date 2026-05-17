package com.donatrack.donaciones.domain.model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RecepcionDonacion {
    private String descripcionGeneral;
    private LocalDate fechaIngreso;
    private List<Bien> bienes;

    public RecepcionDonacion(String descripcionGeneral) {
        this.descripcionGeneral = descripcionGeneral;
        this.fechaIngreso = LocalDate.now(); // Se setea el día de hoy automáticamente
        this.bienes = new ArrayList<>();
    }

    public void agregarBienBruto(Bien b) {
        this.bienes.add(b);
    }

    // Método principal que aplica el Patrón Strategy
    public List<Donacion> procesarRecepcion(EstrategiaSegmentacion estrategia) {
        // El formulario delega la tarea de segmentar a la estrategia recibida por parámetro
        return estrategia.segmentar(this.bienes);
    }

    // Getters y Setters
    public String getDescripcionGeneral() { return descripcionGeneral; }
    public void setDescripcionGeneral(String descripcionGeneral) { this.descripcionGeneral = descripcionGeneral; }

    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public List<Bien> getBienes() { return bienes; }
    public void setBienes(List<Bien> bienes) { this.bienes = bienes; }
}