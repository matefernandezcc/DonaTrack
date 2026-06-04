package com.donatrack.incentivos.infrastructure.controller;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.Mision;
import com.donatrack.incentivos.domain.model.PerfilDonante;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incentivos")
public class IncentivoController {

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
}
