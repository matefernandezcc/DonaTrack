package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.EstadoDonacion;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Donacion {
    private UUID id;
    private EstadoDonacion estado;
    private List<Bien> bienes;
    private Categoria subcategoria; // Obligatoriamente agrupada por subcategoría

    public Donacion(Categoria subcategoria) {
        this.id = UUID.randomUUID();
        this.estado = EstadoDonacion.PENDIENTE; 
        this.bienes = new ArrayList<>();
        this.subcategoria = subcategoria;
    }

    public void agregarBien(Bien b) {
        this.bienes.add(b);
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public EstadoDonacion getEstado() { return estado; }
    public void setEstado(EstadoDonacion estado) { this.estado = estado; }

    public List<Bien> getBienes() { return bienes; }
    public void setBienes(List<Bien> bienes) { this.bienes = bienes; }

    public Categoria getSubcategoria() { return subcategoria; }
    public void setSubcategoria(Categoria subcategoria) { this.subcategoria = subcategoria; }
}