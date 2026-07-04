package com.donatrack.donaciones.infrastructure.adapters.in.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.donatrack.donaciones.domain.entities.persona.Persona;

import java.util.UUID;

@RestController
@RequestMapping("/api/personas")
public class PersonaController {

    @PostMapping
    public ResponseEntity<Persona> crearPersona(@RequestBody Persona persona) {
        return ResponseEntity.ok(persona);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Persona> obtenerPersona(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Persona> actualizarPersona(@PathVariable UUID id, @RequestBody Persona persona) {
        return ResponseEntity.ok(persona);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPersona(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }
}
