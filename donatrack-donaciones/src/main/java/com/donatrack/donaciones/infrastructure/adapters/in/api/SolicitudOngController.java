package com.donatrack.donaciones.infrastructure.adapters.in.api;

import com.donatrack.common.dto.ErrorResponse;
import com.donatrack.donaciones.infrastructure.adapters.in.api.dtos.SolicitudOngDTO;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities.SolicitudOngEntity;
import com.donatrack.donaciones.infrastructure.adapters.out.persistence.repositories.SolicitudOngJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solicitudes-ong")
@Tag(
    name = "Solicitudes ONG",
    description = "Gestión de solicitudes de adhesión de ONGs y Comedores")
public class SolicitudOngController {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  private final SolicitudOngJpaRepository repository;

  public SolicitudOngController(SolicitudOngJpaRepository repository) {
    this.repository = repository;
  }

  @PostConstruct
  public void seedInitialSolicitudes() {
    try {
      if (repository.count() == 0) {
        repository.save(
            new SolicitudOngEntity(
                "Comedor Los Bajitos de Quilmes",
                "30-71458921-4",
                "Av. Calchaquí 1820, Quilmes, Bs. As.",
                "comedor.losbajitos@gmail.com",
                "+54 11 4253-9988",
                "Marta Gómez",
                "Comedor Comunitario",
                "Alimentos no perecederos (arroz, fideos, leche en polvo)",
                "PENDIENTE"));

        repository.save(
            new SolicitudOngEntity(
                "Hogar Niños del Sol",
                "30-68954123-8",
                "Calle 14 N° 520, La Plata",
                "contacto@ninosdelsol.org.ar",
                "+54 221 482-1144",
                "Esteban Rivas",
                "Hogar de Tránsito",
                "Camperas, frazadas y calzado escolar",
                "PENDIENTE"));
      }
    } catch (Exception e) {
      // Fallback si la tabla no está lista aún
    }
  }

  @Operation(summary = "Registrar solicitud de adhesión de ONG")
  @ApiResponse(responseCode = "200", description = "Solicitud registrada con éxito")
  @ApiResponse(
      responseCode = "400",
      description = "Datos inválidos",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping
  public ResponseEntity<SolicitudOngDTO> registrar(@RequestBody Map<String, String> data) {
    String nombre =
        data.containsKey("nombreOng")
            ? data.get("nombreOng")
            : data.getOrDefault("nombreOrganizacion", "ONG sin nombre");
    String tipo =
        data.containsKey("tipoEntidad")
            ? data.get("tipoEntidad")
            : data.getOrDefault("tipo", "Comedor");

    SolicitudOngEntity entity =
        new SolicitudOngEntity(
            nombre,
            data.getOrDefault("cuit", "-"),
            data.getOrDefault("direccion", "-"),
            data.getOrDefault("email", "-"),
            data.getOrDefault("telefono", "-"),
            data.getOrDefault("responsable", "-"),
            tipo,
            data.getOrDefault("necesidades", "-"),
            "PENDIENTE");

    SolicitudOngEntity guardada = repository.save(entity);
    return ResponseEntity.ok(toDto(guardada));
  }

  @Operation(summary = "Listar todas las solicitudes de ONG")
  @ApiResponse(responseCode = "200", description = "Lista de solicitudes")
  @GetMapping
  public ResponseEntity<List<SolicitudOngDTO>> listar() {
    List<SolicitudOngDTO> dtos =
        repository.findAllByOrderByFechaCreacionDesc().stream().map(this::toDto).toList();
    return ResponseEntity.ok(dtos);
  }

  @Operation(summary = "Obtener solicitud por ID")
  @ApiResponse(responseCode = "200", description = "Solicitud encontrada")
  @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
  @GetMapping("/{id}")
  public ResponseEntity<SolicitudOngDTO> obtenerPorId(@PathVariable String id) {
    try {
      UUID uuid = UUID.fromString(id);
      return repository
          .findById(uuid)
          .map(entity -> ResponseEntity.ok(toDto(entity)))
          .orElse(ResponseEntity.notFound().build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @Operation(summary = "Actualizar estado de una solicitud de ONG")
  @ApiResponse(responseCode = "200", description = "Estado actualizado")
  @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
  @PutMapping("/{id}/estado")
  public ResponseEntity<SolicitudOngDTO> actualizarEstado(
      @PathVariable String id,
      @RequestBody(required = false) Map<String, String> body,
      @RequestParam(value = "estado", required = false) String estadoParam) {
    String nuevoEstado =
        (body != null && body.containsKey("estado")) ? body.get("estado") : estadoParam;
    if (nuevoEstado == null || nuevoEstado.isBlank()) {
      nuevoEstado = "APROBADA";
    }

    try {
      UUID uuid = UUID.fromString(id);
      String finalEstado = nuevoEstado;
      return repository
          .findById(uuid)
          .map(
              entity -> {
                entity.setEstado(finalEstado);
                SolicitudOngEntity guardada = repository.save(entity);
                return ResponseEntity.ok(toDto(guardada));
              })
          .orElse(ResponseEntity.notFound().build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  private SolicitudOngDTO toDto(SolicitudOngEntity entity) {
    String fechaStr =
        entity.getFechaCreacion() != null ? entity.getFechaCreacion().format(FORMATTER) : "";
    return new SolicitudOngDTO(
        entity.getId() != null ? entity.getId().toString() : "",
        entity.getNombreOng(),
        entity.getCuit(),
        entity.getDireccion(),
        entity.getEmail(),
        entity.getTelefono(),
        entity.getResponsable(),
        entity.getTipoEntidad(),
        entity.getNecesidades(),
        entity.getEstado(),
        fechaStr);
  }
}
