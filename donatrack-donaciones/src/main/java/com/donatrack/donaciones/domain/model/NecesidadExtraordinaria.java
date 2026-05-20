package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.EstadoNecesidad;
import java.util.ArrayList;
import java.util.List;

public class NecesidadExtraordinaria extends Necesidad {
    private double cantidadRequerida;
    private List<Donacion> donacionesParciales;

    public NecesidadExtraordinaria(String descripcion, Categoria subcategoriaRequerida, double cantidadRequerida) {
        super(descripcion, subcategoriaRequerida);
        this.cantidadRequerida = cantidadRequerida;
        this.donacionesParciales = new ArrayList<>();
    }

    public void acumularDonacionesParciales(Donacion nuevaDonacion) {
        this.donacionesParciales.add(nuevaDonacion);
        double totalAcumulado = 0;

        for (Donacion d : this.donacionesParciales) {
            for (Bien b : d.getBienes()) {
                totalAcumulado += b.getCantidad();
            }
        }

        if (totalAcumulado >= this.cantidadRequerida) {
            this.setEstado(EstadoNecesidad.SATISFECHA);
        }
    }

    // Getters y Setters
    public double getCantidadRequerida() { return cantidadRequerida; }
    public void setCantidadRequerida(double cantidadRequerida) { this.cantidadRequerida = cantidadRequerida; }
    
    public List<Donacion> getDonacionesParciales() { return donacionesParciales; }
}