package com.donatrack.incentivos.domain.entities;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MetricasDonante {

    private UUID donanteId;
    private List<RegistroDonacion> registrosDonacion;
    private HashMap<Mision, YearMonth> misionesCompletadas;

    public MetricasDonante(UUID donanteId) {
        this.donanteId = donanteId;
        this.registrosDonacion = new ArrayList<>();
        this.misionesCompletadas = new HashMap<>();
    }

    public int totalDonacionesHistoricas() {
        return this.registrosDonacion.size();
    }

    public void registrarDonacion(RegistroDonacion donacion) {
        this.registrosDonacion.add(donacion);
    }

    public void registrarMisionCompletada(Mision mision, YearMonth mesCompletado) {
        this.misionesCompletadas.put(mision, mesCompletado);
    }

    public int mesesConsecutivosDonando() {
        //Caso 0: No hay donaciones
        if (registrosDonacion.isEmpty()) {
            return 0;
        }
	
        List<YearMonth> mesesDonando = registrosDonacion.stream()
	    					        .map(RegistroDonacion::getMesDonacion)
	    					        .distinct()
						            .sorted(Comparator.reversed())
						            .collect(Collectors.toList());
	
        //Caso A: Si el mes más reciente no es el actual, la racha está rota
        if (!mesesDonando.get(0).equals(YearMonth.now())) {
            return 0;
        }

        //Caso B: El mes actual si hubo donacion
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
        return this.registrosDonacion.stream().mapToInt(RegistroDonacion::getCantidadBienes).sum();
    }
    
    public int totalDonacionesExitosas() {
        return 0;
        //Hay que incluir en RegistroDonacion, un atributo que contemple si fue exitosa o no (su estado)
        //la donacion. Con un verificador que deberá actualizarlo al momento de cambiar ese estado en la donacion (de pendiente a exitosa)
    }

    public int maxBienesEnUnaDonacion() {
        return this.registrosDonacion.stream().mapToInt(RegistroDonacion::getCantidadBienes).max().orElse(0);
    }

    public Set<String> categoriasUnicasDonadas() {
        return this.registrosDonacion.stream().flatMap(r -> r.getCategorias().stream())
                        .collect(Collectors.toSet());
    }

    public Set<UUID> totalOrganizacionesAyudadas() {
       return null;
    }

    public HashMap<YearMonth, Integer> historialDonacionesPorMes() {
        HashMap<YearMonth, Integer> donacionesPorMes = new HashMap<>();
        for (RegistroDonacion donacion : registrosDonacion) {
            donacionesPorMes.merge(donacion.getMesDonacion(), 1, Integer::sum);
        }
        return donacionesPorMes;
    }

    public YearMonth ultimoMesDonacion() {
        return this.registrosDonacion.stream().map(RegistroDonacion::getMesDonacion)
                        .max(Comparator.naturalOrder()).orElse(null);
    }

    public HashMap<YearMonth, List<Mision>> getMisionesCompletadasPorMes() {
        return misionesCompletadas.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue, 
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList()) 
            ));
    }

}
