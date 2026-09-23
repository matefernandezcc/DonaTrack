package com.donatrack.donaciones.infrastructure.adapters.in.api;

import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.donacion.Archivo;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.importador.ImportadorCSV;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/personas")
@Tag(name = "Personas", description = "CRUD de personas (humanas y jurídicas) e importación CSV")
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

  @Operation(summary = "Importar personas desde archivo CSV", description = "Procesa e inserta personas humanas y jurídicas desde un archivo CSV")
  @ApiResponse(responseCode = "200", description = "Personas importadas correctamente")
  @PostMapping(value = "/importar-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> importarCSV(@RequestParam("file") MultipartFile file) throws IOException {
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body("El archivo CSV está vacío.");
    }
    ImportadorCSV importador = new ImportadorCSV(personaRepository);
    Archivo archivo = new Archivo(file.getOriginalFilename(), file.getBytes());
    importador.importar(archivo);
    return ResponseEntity.ok("CSV procesado e importado con éxito a la base de datos.");
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
    return ResponseEntity.noContent().build();
  }
}
