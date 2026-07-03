package com.donatrack.donaciones.infrastructure.adapters.in.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.necesidades.Necesidad;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/beneficiarios")
public class BeneficiarioController {

    private static final List<Beneficiario> beneficiarios = new java.util.concurrent.CopyOnWriteArrayList<>();

    @PostMapping
    public ResponseEntity<Beneficiario> crearBeneficiario(@RequestBody Beneficiario beneficiario) {
        if (beneficiario.getId() == null) {
            beneficiario.setId(UUID.randomUUID());
        }
        beneficiarios.add(beneficiario);
        return ResponseEntity.ok(beneficiario);
    }

    @GetMapping
    public ResponseEntity<List<Beneficiario>> obtenerTodos() {
        return ResponseEntity.ok(beneficiarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Beneficiario> obtenerBeneficiario(@PathVariable UUID id) {
        return beneficiarios.stream()
                .filter(b -> id.equals(b.getId()))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Beneficiario> actualizarBeneficiario(@PathVariable UUID id, @RequestBody Beneficiario beneficiario) {
        for (int i = 0; i < beneficiarios.size(); i++) {
            if (id.equals(beneficiarios.get(i).getId())) {
                beneficiario.setId(id);
                beneficiarios.set(i, beneficiario);
                return ResponseEntity.ok(beneficiario);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarBeneficiario(@PathVariable UUID id) {
        boolean removed = beneficiarios.removeIf(b -> id.equals(b.getId()));
        if (removed) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // CRUD Necesidades
    @PostMapping("/{id}/necesidades")
    public ResponseEntity<Void> agregarNecesidad(@PathVariable UUID id, @RequestBody Necesidad necesidad) {
        return beneficiarios.stream()
                .filter(b -> id.equals(b.getId()))
                .findFirst()
                .map(b -> {
                    b.registrarNecesidad(necesidad);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/necesidades")
    public ResponseEntity<List<Necesidad>> obtenerNecesidades(@PathVariable UUID id) {
        return beneficiarios.stream()
                .filter(b -> id.equals(b.getId()))
                .findFirst()
                .map(b -> ResponseEntity.ok(b.getNecesidadesDeclaradas()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/necesidades/{idNecesidad}")
    public ResponseEntity<Void> actualizarNecesidad(@PathVariable UUID id, @PathVariable UUID idNecesidad, @RequestBody Necesidad necesidadActualizada) {
        return beneficiarios.stream()
                .filter(b -> id.equals(b.getId()))
                .findFirst()
                .map(b -> // Logic to update the necessity inside the beneficiario's list
                    ResponseEntity.ok().<Void>build())
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/necesidades/{idNecesidad}")
    public ResponseEntity<Void> eliminarNecesidad(@PathVariable UUID id, @PathVariable UUID idNecesidad) {
        return beneficiarios.stream()
                .filter(b -> id.equals(b.getId()))
                .findFirst()
                .map(b -> 
                    // Logic to remove the necessity from the beneficiario's list
                    ResponseEntity.noContent().<Void>build())
                .orElse(ResponseEntity.notFound().build());
    }
}
