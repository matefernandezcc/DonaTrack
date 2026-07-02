package com.donatrack.incentivos.domain.entities;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Objects;
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

    public int totalDonacionesHistoricas() {
        return this.registrosDonacion.size();
    }

    public void registrarDonacion(RegistroDonacion donacion) {
        // Buscar si ya existe un registro con el mismo idDonacion
        for (int i = 0; i < registrosDonacion.size(); i++) {
            if (registrosDonacion.get(i).getIdDonacion().equals(donacion.getIdDonacion())) {
                registrosDonacion.set(i, donacion);
                return;
            }
        }
        this.registrosDonacion.add(donacion);
    }

    public void registrarMisionCompletada(Mision mision, YearMonth mesCompletado) {
        this.misionesCompletadas.put(mision, mesCompletado);
    }

    public int rachaDeMeses() {
        // Caso 0: No hay donaciones
        if (registrosDonacion.isEmpty()) {
            return 0;
        }

        List<YearMonth> mesesDonando = registrosDonacion.stream()
                .map(RegistroDonacion::getMesDonacion)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        // Caso A: Si el mes más reciente no es el actual, la racha está rota
        if (!mesesDonando.get(0).equals(YearMonth.now())) {
            return 0;
        }

        // Caso B: El mes actual si hubo donacion
        int mesesConsecutivos = 1;

        for (int i = 0; i < mesesDonando.size() - 1; i++) {
            YearMonth mesIterado = mesesDonando.get(i);
            YearMonth mesSiguiente = mesesDonando.get(i + 1);

            if (mesIterado.minusMonths(1).equals(mesSiguiente)) {
                mesesConsecutivos++;
            } else {
                break;
            }
        }

        return mesesConsecutivos;
    }

    public int totalBienesDonados() {
        return this.registrosDonacion.stream()
                .mapToInt(RegistroDonacion::getCantidadBienes)
                .sum();
    }

    public int totalDonacionesExitosas() {
        return (int) this.registrosDonacion.stream()
                .filter(RegistroDonacion::esExitosa)
                .count();
    }

    public int maxBienesPorDonacion() {
        return this.registrosDonacion.stream()
                .mapToInt(RegistroDonacion::getCantidadBienes).max().orElse(0);
    }

    public Set<String> categoriasDonadas() {
        return this.registrosDonacion.stream()
                .map(RegistroDonacion::getCategorias)
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    public Set<UUID> organizacionesBeneficiadas() {
        return registrosDonacion.stream()
                .map(RegistroDonacion::getIdEntidadBeneficiaria)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public HashMap<YearMonth, Integer> historialDonacionesPorMes() {
        HashMap<YearMonth, Integer> donacionesPorMes = new HashMap<>();
        for (RegistroDonacion donacion : registrosDonacion) {
            donacionesPorMes.merge(donacion.getMesDonacion(), 1, Integer::sum);
        }
        return donacionesPorMes;
    }

    public YearMonth ultimoMesDonacion() {
        return this.registrosDonacion.stream()
                .map(RegistroDonacion::getMesDonacion)
                .max(Comparator.naturalOrder()).orElse(null);
    }

    public Map<YearMonth, List<Mision>> misionesCompletadasPorMes() {
        return misionesCompletadas.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
    }

    public int totalMisionesCompletadasEn(YearMonth mes) {
        return (int) this.misionesCompletadasPorMes().getOrDefault(mes, Collections.emptyList()).size();
    }
}