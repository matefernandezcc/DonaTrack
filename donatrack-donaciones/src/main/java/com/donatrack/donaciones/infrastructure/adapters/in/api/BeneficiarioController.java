package com.donatrack.donaciones.infrastructure.adapters.in.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.donatrack.donaciones.domain.model.roles.Beneficiario;
import com.donatrack.donaciones.domain.model.necesidades.Necesidad;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/donaciones/beneficiarios")
public class BeneficiarioController {

    @PostMapping
    public ResponseEntity<Beneficiario> crearBeneficiario(@RequestBody Beneficiario beneficiario) {
        return ResponseEntity.ok(beneficiario);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Beneficiario> obtenerBeneficiario(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Beneficiario> actualizarBeneficiario(@PathVariable UUID id, @RequestBody Beneficiario beneficiario) {
        return ResponseEntity.ok(beneficiario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarBeneficiario(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }

    // CRUD Necesidades
    @PostMapping("/{id}/necesidades")
    public ResponseEntity<Void> agregarNecesidad(@PathVariable UUID id, @RequestBody Necesidad necesidad) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/necesidades")
    public ResponseEntity<List<Necesidad>> obtenerNecesidades(@PathVariable UUID id) {
        return ResponseEntity.ok(List.of());
    }
}
