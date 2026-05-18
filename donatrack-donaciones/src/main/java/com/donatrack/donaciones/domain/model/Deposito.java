package com.donatrack.donaciones.domain.model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Deposito {
    private String id;
    private List<Donacion> donacionesDisponibles;
    private String nombre;
    private int capacidadMaxima;
    private Direccion direccion;

    public Deposito(String id, String nombre, int capacidadMaxima, Direccion direccion) {
        this.id = id;
        this.nombre = nombre;
        this.capacidadMaxima = capacidadMaxima;
        this.direccion = direccion;
        this.donacionesDisponibles = new ArrayList<>();
    }

    // --- Implementación de la lógica de negocio ---
    public void auditarVencidos() {
        LocalDate hoy = LocalDate.now();
        
        // Usamos un Iterator para poder remover elementos de la lista de forma segura mientras la recorremos
        Iterator<Donacion> iterador = donacionesDisponibles.iterator();
        
        while (iterador.hasNext()) {
            Donacion donacion = iterador.next();
            boolean tieneVencidos = false;
            
            for (Bien bien : donacion.getBienes()) {
                // Si el bien tiene fecha de vencimiento y ya pasó la fecha de hoy
                if (bien.getFechaVencimiento() != null && bien.getFechaVencimiento().isBefore(hoy)) {
                    tieneVencidos = true;
                    break; // Con un solo bien vencido, apartamos la donación entera
                }
            }
            
            if (tieneVencidos) {
                // Aquí podrías guardar la donación en una lista de "descartados", 
                // pero por lo pronto la retiramos del depósito.
                iterador.remove();
            }
        }
    }

    public void ingresarDonacion(Donacion d) { this.donacionesDisponibles.add(d); }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public Direccion getDireccion() { return direccion; }
    public void setDireccion(Direccion direccion) { this.direccion = direccion; }
    
    public List<Donacion> getDonacionesDisponibles() { return donacionesDisponibles; }
}

