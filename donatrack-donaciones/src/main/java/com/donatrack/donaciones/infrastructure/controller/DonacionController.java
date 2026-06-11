package com.donatrack.donaciones.infrastructure.controller;
import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.roles.Beneficiario;
import com.donatrack.donaciones.domain.service.matchmaking.MatchmakerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/donaciones")
public class DonacionController {

    private final MatchmakerService matchmakerService;
    
    // Asumiendo que existe un DonacionRepository/Service inyectado en un caso real
    public DonacionController(MatchmakerService matchmakerService) {
        this.matchmakerService = matchmakerService;
    }

    @PostMapping
    public ResponseEntity<Donacion> crearDonacion(@RequestBody Donacion donacion) {
        // Lógica de guardado (mocked)
        return ResponseEntity.ok(donacion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Donacion> obtenerDonacion(@PathVariable UUID id) {
        // Lógica de obtención (mocked)
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/estado/asignar")
    public ResponseEntity<Void> asignarDonacion(@PathVariable UUID id, @RequestBody Beneficiario beneficiario) {
        // donacion.asignar(beneficiario)
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}/estado/preparar")
    public ResponseEntity<Void> prepararParaEntrega(@PathVariable UUID id) {
        // donacion.prepararParaEntrega()
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/matchmaking")
    public ResponseEntity<List<Beneficiario>> sugerirBeneficiarios(@PathVariable UUID id) {
        // TODO: obtener donación y beneficiarios disponibles desde repositorios
        return ResponseEntity.ok(List.of());
    }
}
