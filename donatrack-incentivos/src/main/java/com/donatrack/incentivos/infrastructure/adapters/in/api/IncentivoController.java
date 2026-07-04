package com.donatrack.incentivos.infrastructure.adapters.in.api;

import com.donatrack.incentivos.domain.entities.Insignia;
import com.donatrack.incentivos.domain.entities.PerfilDonante;
import com.donatrack.incentivos.domain.entities.RegistroDonacion;
import com.donatrack.incentivos.domain.entities.misiones.Mision;
import com.donatrack.incentivos.application.usecases.RegistrarActividadDonacionUseCase;
import com.donatrack.common.dto.ActividadDonacionDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class IncentivoController {

    private final com.donatrack.incentivos.domain.services.RankingMensualService rankingMensualService;
    private final RegistrarActividadDonacionUseCase registrarActividadDonacionUseCase;

    public IncentivoController(com.donatrack.incentivos.domain.services.RankingMensualService rankingMensualService,
            RegistrarActividadDonacionUseCase registrarActividadDonacionUseCase) {
        this.rankingMensualService = rankingMensualService;
        this.registrarActividadDonacionUseCase = registrarActividadDonacionUseCase;
    }

    @GetMapping("/donantes/{id}/metricas")
    public ResponseEntity<MetricasDonanteDTO> obtenerMetricas(@PathVariable UUID id) {
        PerfilDonante perfil = new PerfilDonante(id); // Mock

        List<RegistroDonacion> todasLasDonaciones = perfil.getMetricas().obtenerTodasLasDonaciones();
        List<RegistroDonacion> exitosas = perfil.getMetricas().obtenerDonacionesExitosas();

        int totalHistorico = todasLasDonaciones.size();
        int racha = calcularRacha(todasLasDonaciones);
        int bienesDonados = todasLasDonaciones.stream().mapToInt(RegistroDonacion::getCantidadBienes).sum();
        int totalExitosas = exitosas.size();
        
        long organizacionesAyudadas = todasLasDonaciones.stream()
                .map(RegistroDonacion::getIdEntidadBeneficiaria)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        Map<YearMonth, Integer> historialMap = new HashMap<>();
        for (RegistroDonacion d : todasLasDonaciones) {
            historialMap.merge(d.getMesDonacion(), 1, Integer::sum);
        }

        MetricasDonanteDTO dto = MetricasDonanteDTO.builder()
                .donanteId(perfil.getDonanteId())
                .totalDonacionesHistoricas(totalHistorico)
                .rachaDeMeses(racha)
                .cantidadBienesDonados(bienesDonados)
                .donacionesExitosas(totalExitosas)
                .totalOrganizacionesAyudadas((int) organizacionesAyudadas)
                .historialDonacionesPorMes(historialMap)
                .posicionRanking(5) // Mock
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/donantes/{id}/misiones")
    public ResponseEntity<List<Mision>> obtenerMisionesDisponibles(@PathVariable UUID id) {
        PerfilDonante perfil = new PerfilDonante(id); // Mock
        List<Mision> respuesta = perfil.getMisionActual() != null ? java.util.List.of(perfil.getMisionActual())
                : new java.util.ArrayList<>();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/donantes/{id}/insignias")
    public ResponseEntity<List<Insignia>> obtenerInsignias(@PathVariable UUID id) {
        PerfilDonante perfil = new PerfilDonante(id); // Mock
        return ResponseEntity.ok(perfil.getInsigniasObtenidas());
    }

    @GetMapping("/ranking/top3")
    public ResponseEntity<List<java.util.Map<String, Object>>> obtenerRankingMensual() {
        java.time.YearMonth mesActual = java.time.YearMonth.now();
        List<PerfilDonante> top3 = rankingMensualService.obtenerTop3Mensual(mesActual);

        List<java.util.Map<String, Object>> response = top3.stream().map(p -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("user", p.getDonanteId().toString());
            map.put("totalDonations", p.getMetricas().obtenerMisionesCompletadasEn(mesActual).size());
            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/donantes/{id}/actividad")
    public ResponseEntity<Void> registrarActividadDonacionExitosa(@PathVariable UUID id,
            @RequestBody ActividadDonacionDTO actividad) {
        registrarActividadDonacionUseCase.ejecutar(id, actividad);
        return ResponseEntity.ok().build();
    }

    private int calcularRacha(List<RegistroDonacion> donaciones) {
        if (donaciones.isEmpty()) {
            return 0;
        }
        List<YearMonth> meses = donaciones.stream()
                .map(RegistroDonacion::getMesDonacion)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        YearMonth mesActual = YearMonth.now();
        if (!meses.contains(mesActual) && !meses.contains(mesActual.minusMonths(1))) {
            return 0;
        }

        int racha = 1;
        for (int i = 0; i < meses.size() - 1; i++) {
            if (meses.get(i).minusMonths(1).equals(meses.get(i + 1))) {
                racha++;
            } else {
                break;
            }
        }
        return racha;
    }
}
