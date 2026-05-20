package com.donatrack.donaciones.domain.model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecepcionDonacion {
    private UUID id;
    private LocalDate fechaRecepcion;
    private Donante donante;
    private Administrador registradoPor;
    private List<Donacion> donacionesSegmentadas;

    public RecepcionDonacion(Donante donante, Administrador registradoPor) {
        this.id = UUID.randomUUID();
        this.fechaRecepcion = LocalDate.now();
        this.donante = donante;
        this.registradoPor = registradoPor;
        this.donacionesSegmentadas = new ArrayList<>();
    }

    public void segmentarBienes(List<Bien> bienesBrutos, EstrategiaSegmentacion estrategia) {
        // Delega la responsabilidad de segmentación a la interfaz EstrategiaSegmentacion
        this.donacionesSegmentadas = estrategia.segmentar(bienesBrutos);
    }

    // --- Getters y Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public LocalDate getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(LocalDate fechaRecepcion) { this.fechaRecepcion = fechaRecepcion; }

    public Donante getDonante() { return donante; }
    public void setDonante(Donante donante) { this.donante = donante; }

    public Administrador getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(Administrador registradoPor) { this.registradoPor = registradoPor; }

    public List<Donacion> getDonacionesSegmentadas() { return donacionesSegmentadas; }
    public void setDonacionesSegmentadas(List<Donacion> donacionesSegmentadas) { this.donacionesSegmentadas = donacionesSegmentadas; }
}