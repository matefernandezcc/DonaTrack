package com.donatrack.incentivos.infrastructure.in.api;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.PerfilDonante;
import com.donatrack.incentivos.domain.model.misiones.Mision;
import com.donatrack.incentivos.infrastructure.out.client.NotificacionClient;
import com.donatrack.incentivos.infrastructure.out.client.NotificacionRequest;
import com.donatrack.common.dto.ActividadDonacionDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.donatrack.incentivos.domain.model.InsigniaObtenidaEvent;

@RestController
@RequestMapping("/incentivos")
public class IncentivoController {

    private final NotificacionClient notificacionClient;
    private final ApplicationEventPublisher eventPublisher;
    private final com.donatrack.incentivos.domain.service.RankingMensualService rankingMensualService;

    public IncentivoController(NotificacionClient notificacionClient,
            ApplicationEventPublisher eventPublisher,
            com.donatrack.incentivos.domain.service.RankingMensualService rankingMensualService) {
        this.notificacionClient = notificacionClient;
        this.eventPublisher = eventPublisher;
        this.rankingMensualService = rankingMensualService;
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
        return ResponseEntity.ok(perfil.getMisionesActivas());
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
        // Mock
        PerfilDonante perfil = new PerfilDonante(id);

        int insigniasAntes = perfil.getInsigniasObtenidas().size();
        com.donatrack.incentivos.domain.model.categoria.CategoriaDonante categoriaAntes = perfil.getCategoria();

        perfil.registrarDonacionExitosa(
                actividad.getCantidadBienes(),
                actividad.getCategorias() != null ? new java.util.HashSet<>(actividad.getCategorias()) : null,
                actividad.getIdEntidadBeneficiaria(),
                java.time.YearMonth.from(actividad.getFecha()));

        int insigniasDespues = perfil.getInsigniasObtenidas().size();
        com.donatrack.incentivos.domain.model.categoria.CategoriaDonante categoriaDespues = perfil.getCategoria();

        boolean misionCompletada = insigniasDespues > insigniasAntes;
        boolean categoriaCambiada = categoriaAntes != categoriaDespues;

        if (misionCompletada) {
            notificacionClient.enviarNotificacion(
                    new NotificacionRequest("donante" + id + "@test.com", "¡Felicidades! Has completado una misión.",
                            "EMAIL"));

            // Publicar el último evento de insignia (la nueva que ganó)
            Insignia ultimaInsignia = perfil.getInsigniasObtenidas().get(insigniasDespues - 1);
            eventPublisher.publishEvent(new InsigniaObtenidaEvent(perfil.getDonanteId(), ultimaInsignia));
        }

        if (categoriaCambiada) {
            notificacionClient.enviarNotificacion(
                    new NotificacionRequest("donante" + id + "@test.com",
                            "¡Increíble! Has subido de categoría a " + categoriaDespues.name(), "EMAIL"));
        }

        return ResponseEntity.ok().build();
    }
}
