package com.donatrack.incentivos.infrastructure.controller;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.Mision;
import com.donatrack.incentivos.domain.model.PerfilDonante;
import com.donatrack.common.dto.ActividadDonacionDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.donatrack.incentivos.infrastructure.client.NotificacionClient;
import com.donatrack.incentivos.infrastructure.client.NotificacionRequest;

@RestController
@RequestMapping("/incentivos")
public class IncentivoController {

    private final NotificacionClient notificacionClient;
    
    public IncentivoController(NotificacionClient notificacionClient) {
        this.notificacionClient = notificacionClient;
    }

    @GetMapping("/donantes/{id}/metricas")
    public ResponseEntity<MetricasDonanteDTO> obtenerMetricas(@PathVariable UUID id) {
        // En un entorno real se trae el PerfilDonante de la base de datos
        PerfilDonante perfil = new PerfilDonante(id); // Mock

        MetricasDonanteDTO dto = MetricasDonanteDTO.builder()
            .donanteId(perfil.getDonanteId())
            .totalDonacionesHistoricas(perfil.getTotalDonacionesHistoricas())
            .mesesConsecutivosDonando(perfil.getMesesConsecutivosDonando())
            .cantidadBienesDonados(perfil.getCantidadBienesDonados())
            .donacionesExitosas(perfil.getDonacionesExitosas())
            .totalOrganizacionesAyudadas(perfil.getOrganizacionesUnicasAyudadas() != null ? perfil.getOrganizacionesUnicasAyudadas().size() : 0)
            .historialDonacionesPorMes(perfil.getHistorialDonacionesPorMes())
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

    @PostMapping("/donantes/{id}/actividad")
    public ResponseEntity<Void> registrarActividadDonacionExitosa(@PathVariable UUID id, @RequestBody ActividadDonacionDTO actividad) {
        // En una app real haríamos:
        // PerfilDonante perfil = repo.findById(id).orElse(new PerfilDonante(id));
        // perfil.registrarDonacionExitosa(actividad);
        // checkearMisiones();
        
        // Simular que el donante completó una misión
        boolean completada = true;
        if (completada) {
            notificacionClient.enviarNotificacion(
                new NotificacionRequest("donante" + id + "@test.com", "¡Felicidades! Has completado una misión.", "EMAIL")
            );
        }

        return ResponseEntity.ok().build();
    }
}
