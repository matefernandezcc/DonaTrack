package com.donatrack.incentivos.infrastructure.in.api;

import com.donatrack.incentivos.domain.entities.Insignia;
import com.donatrack.incentivos.domain.entities.PerfilDonante;
import com.donatrack.incentivos.domain.entities.misiones.Mision;
import com.donatrack.incentivos.application.usecase.RegistrarActividadDonacionUseCase;
import com.donatrack.common.dto.ActividadDonacionDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/incentivos")
public class IncentivoController {

    private final com.donatrack.incentivos.domain.service.RankingMensualService rankingMensualService;
    private final RegistrarActividadDonacionUseCase registrarActividadDonacionUseCase;

    public IncentivoController(com.donatrack.incentivos.domain.service.RankingMensualService rankingMensualService,
            RegistrarActividadDonacionUseCase registrarActividadDonacionUseCase) {
        this.rankingMensualService = rankingMensualService;
        this.registrarActividadDonacionUseCase = registrarActividadDonacionUseCase;
    }

    @GetMapping("/donantes/{id}/metricas")
    public ResponseEntity<MetricasDonanteDTO> obtenerMetricas(@PathVariable UUID id) {
        // En un entorno real se trae el PerfilDonante de la base de datos
        PerfilDonante perfil = new PerfilDonante(id); // Mock

        MetricasDonanteDTO dto = MetricasDonanteDTO.builder()
                .donanteId(perfil.getDonanteId())
                .totalDonacionesHistoricas(perfil.getMetricas().getTotalDonacionesHistoricas())
                .mesesConsecutivosDonando(perfil.getMetricas().getMesesConsecutivosDonando())
                .cantidadBienesDonados(perfil.getMetricas().getCantidadBienesDonados())
                .donacionesExitosas(perfil.getMetricas().getDonacionesExitosas())
                .totalOrganizacionesAyudadas(perfil.getMetricas().getOrganizacionesUnicasAyudadas() != null
                        ? perfil.getMetricas().getOrganizacionesUnicasAyudadas().size()
                        : 0)
                .historialDonacionesPorMes(perfil.getMetricas().getHistorialDonacionesPorMes())
                .posicionRanking(5) // Mock
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/donantes/{id}/misiones")
    public ResponseEntity<List<Mision>> obtenerMisionesDisponibles(@PathVariable UUID id) {
        PerfilDonante perfil = new PerfilDonante(id); // Mock
        List<Mision> respuesta = perfil.getMisionActual() != null ? 
            java.util.List.of(perfil.getMisionActual()) : new java.util.ArrayList<>();
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
            // n8n espera "user" y "totalDonations" (según su Format Podium Message), pasamos el valor real del ranking
            map.put("user", p.getDonanteId().toString());
            map.put("totalDonations", p.getMetricas().getMisionesCompletadasEn(mesActual));
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
}
