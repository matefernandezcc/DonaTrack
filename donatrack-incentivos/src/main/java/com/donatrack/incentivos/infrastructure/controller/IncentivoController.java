package com.donatrack.incentivos.infrastructure.controller;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.Mision;
import com.donatrack.incentivos.domain.model.PerfilDonante;
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
    public ResponseEntity<PerfilDonante> obtenerMetricas(@PathVariable UUID id) {
        // repo.findById(id)
        return ResponseEntity.ok().build();
    }

    @GetMapping("/donantes/{id}/misiones")
    public ResponseEntity<List<Mision>> obtenerMisionesDisponibles(@PathVariable UUID id) {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/donantes/{id}/insignias")
    public ResponseEntity<List<Insignia>> obtenerInsignias(@PathVariable UUID id) {
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/donantes/{id}/actividad")
    public ResponseEntity<Void> registrarActividadDonacionExitosa(@PathVariable UUID id) {
        // repo.findById(id).registrarDonacionExitosa();
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
