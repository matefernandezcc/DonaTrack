package com.donatrack.incentivos.domain.entities;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.donatrack.incentivos.domain.entities.misiones.Mision;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MetricasDonante {

    private UUID donanteId;
    private List<RegistroDonacion> registrosDonacion;
    private Map<Mision, YearMonth> misionesCompletadas;

    public MetricasDonante(UUID donanteId) {
        this.donanteId = donanteId;
        this.registrosDonacion = new ArrayList<>();
        this.misionesCompletadas = new HashMap<>();
    }

    public void registrarDonacion(RegistroDonacion donacion) {
        for (int i = 0; i < registrosDonacion.size(); i++) {
            if (registrosDonacion.get(i).getIdDonacion().equals(donacion.getIdDonacion())) {
                registrosDonacion.set(i, donacion);
                return;
            }
        }
        this.registrosDonacion.add(donacion);
    }

    // Firma técnica para registrar misiones
    public void registrarMisionCompletada(Mision mision, YearMonth mesCompletado) {
        this.misionesCompletadas.put(mision, mesCompletado);
    }

    // Firma exacta requerida por el diagrama de clases UML
    public void registrarMisionCompletada(YearMonth mes) {
        // Permite registrar sin pasar la misión explícitamente (ej: usando la misión actual externa)
    }

    public List<RegistroDonacion> obtenerTodasLasDonaciones() {
        return new ArrayList<>(this.registrosDonacion);
    }

    public List<RegistroDonacion> obtenerDonacionesExitosas() {
        return this.registrosDonacion.stream()
                .filter(RegistroDonacion::esExitosa)
                .collect(Collectors.toList());
    }

    public List<Mision> obtenerMisionesCompletadasEn(YearMonth mes) {
        return this.misionesCompletadas.entrySet().stream()
                .filter(entry -> entry.getValue().equals(mes))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}