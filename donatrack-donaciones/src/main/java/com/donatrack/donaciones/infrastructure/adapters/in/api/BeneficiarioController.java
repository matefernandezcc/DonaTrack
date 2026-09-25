package com.donatrack.donaciones.infrastructure.adapters.in.api;

import com.donatrack.common.dto.ErrorResponse;
import com.donatrack.donaciones.application.ports.out.BeneficiarioRepository;
import com.donatrack.donaciones.domain.entities.necesidades.Necesidad;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/beneficiarios")
@Tag(name = "Beneficiarios", description = "Gestión de entidades beneficiarias y sus necesidades")
public class BeneficiarioController {

  private final BeneficiarioRepository beneficiarioRepository;

  public BeneficiarioController(BeneficiarioRepository beneficiarioRepository) {
    this.beneficiarioRepository = beneficiarioRepository;
  }

  @Operation(
      summary = "Crear beneficiario",
      description = "Registra una nueva entidad beneficiaria")
  @ApiResponse(responseCode = "200", description = "Beneficiario creado")
  @ApiResponse(
      responseCode = "400",
      description = "Datos inválidos",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "409",
      description = "Beneficiario duplicado o conflicto de datos",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping
  public ResponseEntity<Beneficiario> crearBeneficiario(@RequestBody Beneficiario beneficiario) {
    Beneficiario guardado = beneficiarioRepository.guardar(beneficiario);
    return ResponseEntity.ok(guardado);
  }

  @Operation(
      summary = "Listar beneficiarios",
      description = "Obtiene todas las entidades beneficiarias registradas")
  @ApiResponse(responseCode = "200", description = "Lista de beneficiarios")
  @GetMapping
  public ResponseEntity<List<Beneficiario>> obtenerTodos() {
    return ResponseEntity.ok(beneficiarioRepository.buscarTodos());
  }

  @Operation(
      summary = "Obtener beneficiario por ID",
      description = "Devuelve los datos de un beneficiario específico")
  @ApiResponse(responseCode = "200", description = "Beneficiario encontrado")
  @ApiResponse(
      responseCode = "400",
      description = "ID con formato inválido",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Beneficiario no encontrado",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @GetMapping("/{id}")
  public ResponseEntity<Beneficiario> obtenerBeneficiario(@PathVariable UUID id) {
    return beneficiarioRepository
        .buscarPorId(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
      summary = "Actualizar beneficiario",
      description = "Actualiza los datos de un beneficiario existente")
  @ApiResponse(responseCode = "200", description = "Beneficiario actualizado")
  @ApiResponse(
      responseCode = "400",
      description = "Datos o formato inválidos",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Beneficiario no encontrado",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PutMapping("/{id}")
  public ResponseEntity<Beneficiario> actualizarBeneficiario(
      @PathVariable UUID id, @RequestBody Beneficiario beneficiario) {
    return beneficiarioRepository
        .buscarPorId(id)
        .map(
            existente -> {
              beneficiario.setId(id);
              Beneficiario guardado = beneficiarioRepository.guardar(beneficiario);
              return ResponseEntity.ok(guardado);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Eliminar beneficiario", description = "Da de baja una entidad beneficiaria")
  @ApiResponse(responseCode = "204", description = "Beneficiario eliminado")
  @ApiResponse(
      responseCode = "400",
      description = "ID con formato inválido",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Beneficiario no encontrado",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> eliminarBeneficiario(@PathVariable UUID id) {
    if (beneficiarioRepository.buscarPorId(id).isPresent()) {
      beneficiarioRepository.eliminarPorId(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  // CRUD Necesidades
  @Operation(
      summary = "Agregar necesidad",
      description = "Registra una nueva necesidad para un beneficiario")
  @ApiResponse(responseCode = "200", description = "Necesidad registrada")
  @ApiResponse(
      responseCode = "400",
      description = "Datos de necesidad inválidos",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Beneficiario no encontrado",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/{id}/necesidades")
  public ResponseEntity<Void> agregarNecesidad(
      @PathVariable UUID id, @RequestBody Necesidad necesidad) {
    return beneficiarioRepository
        .buscarPorId(id)
        .map(
            b -> {
              b.registrarNecesidad(necesidad);
              beneficiarioRepository.guardar(b);
              return ResponseEntity.ok().<Void>build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
      summary = "Listar necesidades",
      description = "Obtiene todas las necesidades declaradas por un beneficiario")
  @ApiResponse(responseCode = "200", description = "Lista de necesidades")
  @ApiResponse(
      responseCode = "400",
      description = "ID con formato inválido",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Beneficiario no encontrado",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @GetMapping("/{id}/necesidades")
  public ResponseEntity<List<Necesidad>> obtenerNecesidades(@PathVariable UUID id) {
    return beneficiarioRepository
        .buscarPorId(id)
        .map(b -> ResponseEntity.ok(b.getNecesidadesDeclaradas()))
        .orElse(ResponseEntity.notFound().build());
  }
}
