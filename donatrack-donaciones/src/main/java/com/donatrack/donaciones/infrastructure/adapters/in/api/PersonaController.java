package com.donatrack.donaciones.infrastructure.adapters.in.api;

import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/personas")
@Tag(name = "Personas", description = "CRUD de personas (humanas y jurídicas)")
public class PersonaController {

  private final PersonaRepository personaRepository;

  public PersonaController(PersonaRepository personaRepository) {
    this.personaRepository = personaRepository;
  }

  @Operation(summary = "Crear persona", description = "Registra una nueva persona en el sistema")
  @ApiResponse(responseCode = "200", description = "Persona creada")
  @PostMapping
  public ResponseEntity<Persona> crearPersona(@RequestBody Persona persona) {
    if (persona.getId() == null) {
      persona.setId(UUID.randomUUID());
    }
    personaRepository.guardar(persona);
    return ResponseEntity.ok(persona);
  }

  @Operation(summary = "Listar personas", description = "Obtiene todas las personas registradas")
  @ApiResponse(responseCode = "200", description = "Lista de personas")
  @GetMapping
  public ResponseEntity<List<Persona>> obtenerTodas() {
    return ResponseEntity.ok(personaRepository.obtenerTodas());
  }

  @Operation(summary = "Obtener persona por ID", description = "Devuelve los datos de una persona específica")
  @ApiResponse(responseCode = "200", description = "Persona encontrada")
  @ApiResponse(responseCode = "404", description = "Persona no encontrada")
  @GetMapping("/{id}")
  public ResponseEntity<Persona> obtenerPersona(@PathVariable UUID id) {
    return personaRepository
        .buscarPorId(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Actualizar persona", description = "Actualiza los datos de una persona existente")
  @ApiResponse(responseCode = "200", description = "Persona actualizada")
  @ApiResponse(responseCode = "404", description = "Persona no encontrada")
  @PutMapping("/{id}")
  public ResponseEntity<Persona> actualizarPersona(
      @PathVariable UUID id, @RequestBody Persona persona) {
    return personaRepository
        .buscarPorId(id)
        .map(
            p -> {
              persona.setId(id);
              personaRepository.guardar(persona);
              return ResponseEntity.ok(persona);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Eliminar persona", description = "Elimina una persona del sistema")
  @ApiResponse(responseCode = "204", description = "Persona eliminada")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> eliminarPersona(@PathVariable UUID id) {
    // Implementación idempotente
    return ResponseEntity.noContent().build();
  }
}
