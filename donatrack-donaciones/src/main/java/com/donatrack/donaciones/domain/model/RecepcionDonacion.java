package com.donatrack.donaciones.domain.model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

public class RecepcionDonacion {
    @Getter private UUID id;
    @Getter @Setter private LocalDate fechaRecepcion;
    @Getter @Setter private Donante donante;
    @Getter @Setter private Administrador registradoPor;
    @Getter private List<Donacion> donacionesSegmentadas;

    public RecepcionDonacion(Donante donante, Administrador registradoPor) {
        this.id = UUID.randomUUID();
        this.fechaRecepcion = LocalDate.now();
        this.donante = donante;
        this.registradoPor = registradoPor;
        this.donacionesSegmentadas = new ArrayList<>();
    }
}