package com.donatrack.donaciones.domain.entities.necesidades;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.Bien;
import com.donatrack.donaciones.domain.entities.enums.EstadoNecesidad;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PeriodoNecesidad {
    private UUID id;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoNecesidad estado;
    private List<Donacion> donacionesAsignadas;

    public PeriodoNecesidad(LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = UUID.randomUUID();
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = EstadoNecesidad.ABIERTA;
        this.donacionesAsignadas = new ArrayList<>();
    }

    public double cantidadAcumulada() {
        return donacionesAsignadas.stream()
                .flatMap(d -> d.getBienes().stream())
                .mapToDouble(Bien::getCantidad)
                .sum();
    }

    public Boolean estaCubierta(double cantidadObjetivo) {
        return cantidadAcumulada() >= cantidadObjetivo;
    }

    public void asignarDonacion(Donacion d, double objetivo) {
        this.donacionesAsignadas.add(d);
        d.cambiarEstado(EstadoDonacion.ASIGNADA, "Asignada a período de necesidad recurrente", null);
        if (estaCubierta(objetivo)) {
            this.estado = EstadoNecesidad.SATISFECHA;
        }
    }
}
