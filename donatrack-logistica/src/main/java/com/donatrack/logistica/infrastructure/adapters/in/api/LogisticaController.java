package com.donatrack.logistica.infrastructure.adapters.in.api;

import com.donatrack.logistica.application.ports.in.ListarItemsPendientesPort;
import com.donatrack.logistica.application.ports.in.IniciarRutaUseCase;
import com.donatrack.logistica.application.ports.in.ProcesarCallbackPlanificacionUseCase;
import com.donatrack.logistica.application.ports.in.ProcesarPlanificacionesPendientesUseCase;
import com.donatrack.logistica.application.ports.out.CamionRepositoryPort;
import com.donatrack.logistica.application.ports.out.ChoferRepositoryPort;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.application.usecases.ConfirmarRecepcionUseCase;
import com.donatrack.logistica.application.usecases.ReportarFallaEntregaUseCase;
import com.donatrack.logistica.domain.entities.reparto.*;
import com.donatrack.logistica.domain.entities.entregas.*;
import com.donatrack.logistica.domain.entities.planificacion.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Logística", description = "Gestión de rutas, camiones, choferes, entregas y planificación")
public class LogisticaController {

    private final ListarItemsPendientesPort listarItemsPendientesPort;
    private final CamionRepositoryPort camionRepository;
    private final ChoferRepositoryPort choferRepository;
    private final RutaDeRepartoRepositoryPort rutaRepository;
    private final IniciarRutaUseCase iniciarRutaUseCase;
    private final ConfirmarRecepcionUseCase confirmarRecepcionUseCase;
    private final ReportarFallaEntregaUseCase reportarFallaEntregaUseCase;
    private final ProcesarPlanificacionesPendientesUseCase planificacionUseCase;
    private final ProcesarCallbackPlanificacionUseCase procesarCallbackPlanificacionUseCase;

    public LogisticaController(ListarItemsPendientesPort listarItemsPendientesPort,
            CamionRepositoryPort camionRepository,
            ChoferRepositoryPort choferRepository,
            RutaDeRepartoRepositoryPort rutaRepository,
            IniciarRutaUseCase iniciarRutaUseCase,
            ConfirmarRecepcionUseCase confirmarRecepcionUseCase,
            ReportarFallaEntregaUseCase reportarFallaEntregaUseCase,
            ProcesarPlanificacionesPendientesUseCase planificacionUseCase,
            ProcesarCallbackPlanificacionUseCase procesarCallbackPlanificacionUseCase) {
        this.listarItemsPendientesPort = listarItemsPendientesPort;
        this.camionRepository = camionRepository;
        this.choferRepository = choferRepository;
        this.rutaRepository = rutaRepository;
        this.iniciarRutaUseCase = iniciarRutaUseCase;
        this.confirmarRecepcionUseCase = confirmarRecepcionUseCase;
        this.reportarFallaEntregaUseCase = reportarFallaEntregaUseCase;
        this.planificacionUseCase = planificacionUseCase;
        this.procesarCallbackPlanificacionUseCase = procesarCallbackPlanificacionUseCase;
    }

    @Operation(summary = "Listar ítems de planificación pendientes", description = "Devuelve todos los ítems de donación pendientes de ser incluidos en una ruta")
    @ApiResponse(responseCode = "200", description = "Lista de ítems pendientes")
    @GetMapping("/planificacion/pendientes")
    public ResponseEntity<List<ItemPlanificacion>> obtenerTodos() {
        return ResponseEntity.ok(listarItemsPendientesPort.listar());
    }

    // --- CRUD Camiones ---
    @Operation(summary = "Crear camión", description = "Registra un nuevo camión en el sistema de logística")
    @ApiResponse(responseCode = "200", description = "Camión creado exitosamente")
    @PostMapping("/camiones")
    public ResponseEntity<Void> crearCamion(@RequestBody Camion camion) {
        camionRepository.guardar(camion);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Listar camiones", description = "Obtiene todos los camiones registrados")
    @ApiResponse(responseCode = "200", description = "Lista de camiones")
    @GetMapping("/camiones")
    public ResponseEntity<List<Camion>> listarCamiones() {
        return ResponseEntity.ok(camionRepository.obtenerTodos());
    }

    @Operation(summary = "Actualizar camión", description = "Actualiza los datos de un camión existente identificado por su patente")
    @ApiResponse(responseCode = "200", description = "Camión actualizado")
    @PutMapping("/camiones/{patente}")
    public ResponseEntity<Void> actualizarCamion(@PathVariable String patente, @RequestBody Camion camion) {
        camion.setPatente(patente);
        camionRepository.guardar(camion);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Eliminar camión", description = "Da de baja un camión por su patente")
    @ApiResponse(responseCode = "200", description = "Camión eliminado")
    @DeleteMapping("/camiones/{patente}")
    public ResponseEntity<Void> eliminarCamion(@PathVariable String patente) {
        camionRepository.eliminar(patente);
        return ResponseEntity.ok().build();
    }

    // --- CRUD Choferes ---
    @Operation(summary = "Crear chofer", description = "Registra un nuevo chofer en el sistema")
    @ApiResponse(responseCode = "200", description = "Chofer creado exitosamente")
    @PostMapping("/choferes")
    public ResponseEntity<Void> crearChofer(@RequestBody Chofer chofer) {
        choferRepository.guardar(chofer);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Listar choferes", description = "Obtiene todos los choferes registrados")
    @ApiResponse(responseCode = "200", description = "Lista de choferes")
    @GetMapping("/choferes")
    public ResponseEntity<List<Chofer>> listarChoferes() {
        return ResponseEntity.ok(choferRepository.obtenerTodos());
    }

    @Operation(summary = "Eliminar chofer", description = "Da de baja un chofer por su legajo")
    @ApiResponse(responseCode = "200", description = "Chofer eliminado")
    @DeleteMapping("/choferes/{legajo}")
    public ResponseEntity<Void> eliminarChofer(@PathVariable String legajo) {
        choferRepository.eliminar(legajo);
        return ResponseEntity.ok().build();
    }

    // --- Control de Rutas ---
    @Operation(summary = "Iniciar ruta", description = "Marca una ruta como iniciada y asigna el chofer responsable")
    @ApiResponse(responseCode = "200", description = "Ruta iniciada correctamente")
    @PostMapping("/rutas/{id}/iniciar")
    public ResponseEntity<Void> iniciarRuta(@PathVariable UUID id, @RequestBody IniciarRutaRequest request) {
        iniciarRutaUseCase.iniciarRuta(id, request.getLegajoChofer());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Listar rutas", description = "Obtiene todas las rutas de reparto")
    @ApiResponse(responseCode = "200", description = "Lista de rutas")
    @GetMapping("/rutas")
    public ResponseEntity<List<RutaDeReparto>> listarRutas() {
        return ResponseEntity.ok(rutaRepository.obtenerTodas());
    }

    @Operation(summary = "Obtener ruta por ID", description = "Devuelve los detalles de una ruta de reparto específica")
    @ApiResponse(responseCode = "200", description = "Ruta encontrada")
    @ApiResponse(responseCode = "404", description = "Ruta no encontrada")
    @GetMapping("/rutas/{id}")
    public ResponseEntity<RutaDeReparto> obtenerRuta(@PathVariable UUID id) {
        return rutaRepository.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Confirmar y fallar entregas ---
    @Operation(summary = "Confirmar entrega", description = "Registra la confirmación de recepción de una donación por parte del beneficiario")
    @ApiResponse(responseCode = "200", description = "Entrega confirmada")
    @PostMapping("/entregas/{idDonacion}/confirmar")
    public ResponseEntity<Void> confirmarEntrega(@PathVariable UUID idDonacion,
            @RequestBody ConfirmarRecepcionRequest request) {
        confirmarRecepcionUseCase.procesar(idDonacion, request.getFotos(), request.getPatenteCamion());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reportar falla de entrega", description = "Registra una entrega fallida con su motivo y si puede replanificarse")
    @ApiResponse(responseCode = "200", description = "Falla registrada")
    @PostMapping("/entregas/{idDonacion}/falla")
    public ResponseEntity<Void> fallarEntrega(@PathVariable UUID idDonacion,
            @RequestBody ReportarFallaRequest request) {
        reportarFallaEntregaUseCase.procesar(idDonacion, request.getMotivo(), request.isPuedeReplanificarse());
        return ResponseEntity.ok().build();
    }

    // --- Planificación ---
    @Operation(summary = "Ejecutar planificación manual", description = "Dispara manualmente el procesamiento de planificaciones pendientes (lotes de 100)")
    @ApiResponse(responseCode = "200", description = "Planificación ejecutada")
    @PostMapping("/planificacion/ejecutar")
    public ResponseEntity<Void> ejecutarPlanificacionManual() {
        planificacionUseCase.procesarPlanificacionesPendientes();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Callback de planificación", description = "URL de callback donde el proveedor externo notifica el resultado de la planificación de rutas")
    @ApiResponse(responseCode = "200", description = "Callback procesado exitosamente")
    @PostMapping("/planificacion/callback")
    public ResponseEntity<Void> procesarCallbackPlanificacion(@RequestBody com.donatrack.logistica.infrastructure.adapters.in.api.dto.CallbackPlanificacionRequest request) {
        procesarCallbackPlanificacionUseCase.procesarCallback(request.getIdSolicitud(), request.getRutas());
        return ResponseEntity.ok().build();
    }

    // --- DTOs estáticos ---
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class IniciarRutaRequest {
        private String legajoChofer;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    public static class ConfirmarRecepcionRequest {
        private List<String> fotos;
        private String patenteCamion;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    public static class ReportarFallaRequest {
        private String motivo;
        private boolean puedeReplanificarse;
    }
}
