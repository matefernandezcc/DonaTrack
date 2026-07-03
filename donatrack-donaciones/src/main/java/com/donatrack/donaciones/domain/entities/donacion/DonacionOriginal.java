package com.donatrack.donaciones.domain.entities.donacion;

import com.donatrack.donaciones.domain.entities.roles.Donante;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonacionOriginal {
    private UUID id;
    private String descripcionGeneral;
    private LocalDate fechaRecepcion;
    private Donante donante;
    private List<Donacion> donacionesSegmentadas;
    private String usuarioId;

    public DonacionOriginal() {
        this.id = UUID.randomUUID();
        this.fechaRecepcion = LocalDate.now();
        this.donacionesSegmentadas = new ArrayList<>();
    }

    public DonacionOriginal(
            String descripcionGeneral,
            Donante donante,
            String usuarioId) {
        this.id = UUID.randomUUID();
        this.descripcionGeneral = descripcionGeneral;
        this.fechaRecepcion = LocalDate.now();
        this.donante = donante;
        this.usuarioId = usuarioId;
        this.donacionesSegmentadas = new ArrayList<>();
    }

    public void segmentarBienes(List<Bien> bienesBrutos, com.donatrack.donaciones.application.usecases.ProcesadorCargaInicial procesador) {
        // Ejecuta la segmentación y guarda las donaciones resultantes
        this.donacionesSegmentadas = procesador.procesar(bienesBrutos);
    }
}
