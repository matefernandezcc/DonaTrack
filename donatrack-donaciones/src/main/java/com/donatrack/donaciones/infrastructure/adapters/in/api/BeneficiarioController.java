package com.donatrack.donaciones.infrastructure.adapters.in.api;

import com.donatrack.donaciones.application.ports.out.BeneficiarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.necesidades.Necesidad;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/beneficiarios")
@Tag(name = "Beneficiarios", description = "Gestión de entidades beneficiarias y sus necesidades")
public class BeneficiarioController {

    private final BeneficiarioRepository beneficiarioRepository;

    public BeneficiarioController(BeneficiarioRepository beneficiarioRepository) {
        this.beneficiarioRepository = beneficiarioRepository;
    }

    @Operation(summary = "Crear beneficiario", description = "Registra una nueva entidad beneficiaria")
    @ApiResponse(responseCode = "200", description = "Beneficiario creado")
    @PostMapping
    public ResponseEntity<Beneficiario> crearBeneficiario(@RequestBody Beneficiario beneficiario) {
        if (beneficiario.getId() == null) {
            beneficiario.setId(UUID.randomUUID());
        }
        beneficiarioRepository.guardar(beneficiario);
        return ResponseEntity.ok(beneficiario);
    }

    @Operation(summary = "Listar beneficiarios", description = "Obtiene todas las entidades beneficiarias registradas")
    @ApiResponse(responseCode = "200", description = "Lista de beneficiarios")
    @GetMapping
    public ResponseEntity<List<Beneficiario>> obtenerTodos() {
        return ResponseEntity.ok(beneficiarioRepository.buscarTodos());
    }

    @Operation(summary = "Obtener beneficiario por ID", description = "Devuelve los datos de un beneficiario específico")
    @ApiResponse(responseCode = "200", description = "Beneficiario encontrado")
    @ApiResponse(responseCode = "404", description = "Beneficiario no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<Beneficiario> obtenerBeneficiario(@PathVariable UUID id) {
        return beneficiarioRepository.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar beneficiario", description = "Actualiza los datos de un beneficiario existente")
    @ApiResponse(responseCode = "200", description = "Beneficiario actualizado")
    @ApiResponse(responseCode = "404", description = "Beneficiario no encontrado")
    @PutMapping("/{id}")
    public ResponseEntity<Beneficiario> actualizarBeneficiario(@PathVariable UUID id, @RequestBody Beneficiario beneficiario) {
        return beneficiarioRepository.buscarPorId(id).map(existing -> {
            beneficiario.setId(id);
            beneficiarioRepository.guardar(beneficiario);
            return ResponseEntity.ok(beneficiario);
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar beneficiario", description = "Da de baja una entidad beneficiaria")
    @ApiResponse(responseCode = "204", description = "Beneficiario eliminado")
    @ApiResponse(responseCode = "404", description = "Beneficiario no encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarBeneficiario(@PathVariable UUID id) {
        return beneficiarioRepository.buscarPorId(id).map(existing -> {
            beneficiarioRepository.eliminar(id);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // CRUD Necesidades
    @Operation(summary = "Agregar necesidad", description = "Registra una nueva necesidad para un beneficiario")
    @ApiResponse(responseCode = "200", description = "Necesidad registrada")
    @ApiResponse(responseCode = "404", description = "Beneficiario no encontrado")
    @PostMapping("/{id}/necesidades")
    public ResponseEntity<Void> agregarNecesidad(@PathVariable UUID id, @RequestBody Necesidad necesidad) {
        return beneficiarioRepository.buscarPorId(id)
                .map(b -> {
                    b.registrarNecesidad(necesidad);
                    beneficiarioRepository.guardar(b);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar necesidades", description = "Obtiene todas las necesidades declaradas por un beneficiario")
    @ApiResponse(responseCode = "200", description = "Lista de necesidades")
    @ApiResponse(responseCode = "404", description = "Beneficiario no encontrado")
    @GetMapping("/{id}/necesidades")
    public ResponseEntity<List<Necesidad>> obtenerNecesidades(@PathVariable UUID id) {
        return beneficiarioRepository.buscarPorId(id)
                .map(b -> ResponseEntity.ok(b.getNecesidadesDeclaradas()))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar necesidad", description = "Actualiza una necesidad específica de un beneficiario")
    @ApiResponse(responseCode = "200", description = "Necesidad actualizada")
    @ApiResponse(responseCode = "404", description = "Beneficiario o necesidad no encontrada")
    @PutMapping("/{id}/necesidades/{idNecesidad}")
    public ResponseEntity<Void> actualizarNecesidad(@PathVariable UUID id, @PathVariable UUID idNecesidad, @RequestBody Necesidad necesidadActualizada) {
        return beneficiarioRepository.buscarPorId(id)
                .map(b -> {
                    List<Necesidad> necesidades = b.getNecesidadesDeclaradas();
                    for (int i = 0; i < necesidades.size(); i++) {
                        if (idNecesidad.equals(necesidades.get(i).getId())) {
                            necesidadActualizada.setId(idNecesidad);
                            necesidades.set(i, necesidadActualizada);
                            beneficiarioRepository.guardar(b);
                            return ResponseEntity.ok().<Void>build();
                        }
                    }
                    return ResponseEntity.notFound().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar necesidad", description = "Elimina una necesidad de un beneficiario")
    @ApiResponse(responseCode = "204", description = "Necesidad eliminada")
    @ApiResponse(responseCode = "404", description = "Beneficiario o necesidad no encontrada")
    @DeleteMapping("/{id}/necesidades/{idNecesidad}")
    public ResponseEntity<Void> eliminarNecesidad(@PathVariable UUID id, @PathVariable UUID idNecesidad) {
        return beneficiarioRepository.buscarPorId(id)
                .map(b -> {
                    boolean removed = b.getNecesidadesDeclaradas()
                            .removeIf(n -> idNecesidad.equals(n.getId()));
                    if (removed) {
                        beneficiarioRepository.guardar(b);
                        return ResponseEntity.noContent().<Void>build();
                    }
                    return ResponseEntity.notFound().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
