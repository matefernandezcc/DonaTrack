package com.donatrack.donaciones.infrastructure.adapters.in.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.donatrack.donaciones.domain.entities.persona.Persona;

import java.util.UUID;

@RestController
@RequestMapping("/api/personas")
@Tag(name = "Personas", description = "CRUD de personas (humanas y jurídicas)")
public class PersonaController {

    @Operation(summary = "Crear persona", description = "Registra una nueva persona en el sistema")
    @ApiResponse(responseCode = "200", description = "Persona creada")
    @PostMapping
    public ResponseEntity<Persona> crearPersona(@RequestBody Persona persona) {
        return ResponseEntity.ok(persona);
    }

    @Operation(summary = "Obtener persona por ID", description = "Devuelve los datos de una persona específica")
    @ApiResponse(responseCode = "200", description = "Persona encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<Persona> obtenerPersona(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Actualizar persona", description = "Actualiza los datos de una persona existente")
    @ApiResponse(responseCode = "200", description = "Persona actualizada")
    @PutMapping("/{id}")
    public ResponseEntity<Persona> actualizarPersona(@PathVariable UUID id, @RequestBody Persona persona) {
        return ResponseEntity.ok(persona);
    }

    @Operation(summary = "Eliminar persona", description = "Elimina una persona del sistema")
    @ApiResponse(responseCode = "204", description = "Persona eliminada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPersona(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }
}
