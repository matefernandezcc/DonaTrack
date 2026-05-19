package com.donatrack.donaciones.domain.model;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.donatrack.donaciones.domain.enums.EstadoDonacion;

public class Donacion {
    private UUID id;
    private EstadoDonacion estado;
    private List<Bien> bienes;
    private Categoria subCategoria; 
    private List<HistorialEstado> historial;
    private Date fechaVencimiento;
    private Beneficiario EntidadAsignada;
    private List<Foto> fotos;

    public Donacion(Categoria Categoria) {
        this.id = UUID.randomUUID();
        this.estado = EstadoDonacion.PENDIENTE; 
        this.bienes = new ArrayList<>();
        this.subCategoria = Categoria;
        this.historial = new ArrayList<>(); 
    }

    public void agregarBien(Bien b) {
        this.bienes.add(b);
    }
    
    public void registrarCambioEstado(HistorialEstado nuevoRegistro) {
        this.historial.add(nuevoRegistro);
        // Cuando agregamos un historial nuevo, actualizamos el estado actual de la donación
        this.estado = nuevoRegistro.getEstado(); 
    }

    // --- Getters y Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public EstadoDonacion getEstado() { return estado; }
    public void setEstado(EstadoDonacion estado) { this.estado = estado; }

    public List<Bien> getBienes() { return bienes; }
    public void setBienes(List<Bien> bienes) { this.bienes = bienes; }

    public Categoria getSubcategoria() { return subCategoria; }
    public void setSubcategoria(Categoria Categoria) { this.subCategoria = Categoria; }

    public List<HistorialEstado> getHistorial() { return historial; }
    public void setHistorial(List<HistorialEstado> historial) { this.historial = historial; }

    public Date getFechaVencimiento() { return fechaVencimiento; }

    public Beneficiario getEntidadAsignada() { return EntidadAsignada; }
    public void setEntidadAsignada(Beneficiario entidadAsignada) { EntidadAsignada = entidadAsignada; }

    public List<Foto> getFotos() { return fotos; }
    public void addFoto(Foto foto) { this.fotos.add(foto); }
}