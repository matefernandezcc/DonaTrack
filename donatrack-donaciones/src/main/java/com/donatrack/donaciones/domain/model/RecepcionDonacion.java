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

    /*
    public void segmentarBienes(List<Bien> bienesBrutos, EstrategiaSegmentacion estrategia) {
        // Delega la responsabilidad de segmentación a la interfaz EstrategiaSegmentacion
        this.donacionesSegmentadas = estrategia.segmentar(bienesBrutos);
    }

    No pondria este método:
    1. Regla de negocio: La segmentacion interna se hace automaticamente. Aca recibis una lista de
    bienes y tenes que elegir la estrategia de segmentacion.
    2. Incompatabilidad en la aplicacion de estrategias. Suponiendo que recibis una lista de bienes
    de perecederos, muebles y ropa, estas obligado a elegir una estrategia de segmentacion. Sin embargo,
    no se puede aplicar segmentacion por vencimiento a ropa, ni segmentación por estado a alimentos.

    ALTERNATIVA:
    Ir segmentando los bienes mediante filtros. Primero si o si se agrupan por subcategoria (lista de muebles,
    lista de ropa, lista de fideos, etc.), luego por vencimiento (este paso se salta si no tiene vencimiento)
    y por ultimo por estado (solo para muebles).
    De esta manera al final de los 3 filtros, retorna una lista con lista de bienes, que posteriormente
    cada lista de bienes resultara en una donacion.

    Ver en: SegmentadorDeBienes.java
    NOTA: Me faltan bastantes cambios aún

     */

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