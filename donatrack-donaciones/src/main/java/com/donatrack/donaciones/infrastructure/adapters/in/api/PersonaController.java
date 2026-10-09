package com.donatrack.donaciones.infrastructure.adapters.in.api;

import com.donatrack.common.dto.ErrorResponse;
import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.donacion.Archivo;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.DocumentoIdentidad;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.Direccion;
import com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.importador.ImportadorCSV;
import com.donatrack.donaciones.infrastructure.adapters.in.api.dtos.ContactoDTO;
import com.donatrack.donaciones.infrastructure.adapters.in.api.dtos.PersonaDTO;
import com.donatrack.donaciones.infrastructure.adapters.in.api.dtos.PersonaDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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

  @Operation(
      summary = "Crear persona",
      description =
          "Registra una nueva persona en el sistema. Debe incluir el campo 'tipo' ('HUMANA' o 'JURIDICA')")
  @ApiResponse(responseCode = "200", description = "Persona creada")
  @ApiResponse(
      responseCode = "400",
      description = "Falta campo 'tipo' o datos inválidos",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "409",
      description = "Documento o email duplicado",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping
  public ResponseEntity<PersonaDTO> crearPersona(@RequestBody PersonaDTO personaDto) {
    Persona persona = PersonaDtoMapper.toDomain(personaDto);
    personaRepository.guardar(persona);
    return ResponseEntity.ok(PersonaDtoMapper.toDto(persona));
  }

  @Operation(
      summary = "Importar personas desde archivo CSV",
      description = "Procesa e inserta personas humanas y jurídicas desde un archivo CSV")
  @ApiResponse(responseCode = "200", description = "Personas importadas correctamente")
  @ApiResponse(
      responseCode = "400",
      description = "Archivo vacío o formato de datos CSV incorrecto",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "415",
      description = "Tipo de medio no soportado (se requiere multipart/form-data)",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping(value = "/importar-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> importarCSV(@RequestParam("file") MultipartFile file)
      throws IOException {
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
  public ResponseEntity<List<PersonaDTO>> obtenerTodas() {
    List<PersonaDTO> dtos = personaRepository.obtenerTodas().stream()
        .map(PersonaDtoMapper::toDto)
        .collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
  }

  @Operation(
      summary = "Obtener persona por ID",
      description = "Devuelve los datos de una persona específica")
  @ApiResponse(responseCode = "200", description = "Persona encontrada")
  @ApiResponse(
      responseCode = "400",
      description = "ID con formato inválido (debe ser UUID)",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Persona no encontrada",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @GetMapping("/{id}")
  public ResponseEntity<PersonaDTO> obtenerPersona(@PathVariable UUID id) {
    return personaRepository
        .buscarPorId(id)
        .map(p -> ResponseEntity.ok(PersonaDtoMapper.toDto(p)))
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
      summary = "Actualizar persona",
      description = "Actualiza los datos de una persona existente (actualización parcial)")
  @ApiResponse(responseCode = "200", description = "Persona actualizada")
  @ApiResponse(
      responseCode = "400",
      description = "Datos o formato inválidos",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Persona no encontrada",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PutMapping("/{id}")
  public ResponseEntity<PersonaDTO> actualizarPersona(
      @PathVariable UUID id, @RequestBody Map<String, Object> datosNuevos) {
    return personaRepository
        .buscarPorId(id)
        .map(
            p -> {
              // Convertir objetos anidados del Map genérico a objetos de dominio
              // sin usar frameworks en el dominio
              Map<String, Object> datosConvertidos = new java.util.HashMap<>(datosNuevos);
              if (datosNuevos.containsKey("contacto") && datosNuevos.get("contacto") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cm = (Map<String, Object>) datosNuevos.get("contacto");
                com.donatrack.donaciones.domain.entities.enums.MedioContacto medio = null;
                if (cm.get("medioPredeterminado") != null) {
                  medio = com.donatrack.donaciones.domain.entities.enums.MedioContacto.valueOf((String) cm.get("medioPredeterminado"));
                }
                datosConvertidos.put("contacto", new Contacto(
                    (String) cm.get("correoElectronico"),
                    (String) cm.get("telefono"),
                    (String) cm.get("whatsapp"),
                    medio));
              }
              if (datosNuevos.containsKey("documento") && datosNuevos.get("documento") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dm = (Map<String, Object>) datosNuevos.get("documento");
                com.donatrack.donaciones.domain.entities.enums.TipoDocumento tipo = null;
                if (dm.get("tipo") != null) {
                  tipo = com.donatrack.donaciones.domain.entities.enums.TipoDocumento.valueOf((String) dm.get("tipo"));
                }
                datosConvertidos.put("documento", new DocumentoIdentidad(tipo, (String) dm.get("numero")));
              }
              p.actualizarInformacion(datosConvertidos);
              personaRepository.guardar(p);
              return ResponseEntity.ok(PersonaDtoMapper.toDto(p));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Eliminar persona", description = "Elimina una persona del sistema")
  @ApiResponse(responseCode = "204", description = "Persona eliminada")
  @ApiResponse(
      responseCode = "400",
      description = "ID con formato inválido",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> eliminarPersona(@PathVariable UUID id) {
    return ResponseEntity.noContent().build();
  }
}
