package com.donatrack.logistica.infrastructure.adapters.in.api;

import com.donatrack.logistica.application.ports.in.ListarItemsPendientesPort;
import com.donatrack.logistica.application.ports.in.IniciarRutaUseCase;
import com.donatrack.logistica.application.ports.in.ProcesarPlanificacionesPendientesUseCase;
import com.donatrack.logistica.application.ports.out.CamionRepositoryPort;
import com.donatrack.logistica.application.ports.out.ChoferRepositoryPort;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.application.usecases.ConfirmarRecepcionUseCase;
import com.donatrack.logistica.application.usecases.ReportarFallaEntregaUseCase;
import com.donatrack.logistica.domain.entities.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class LogisticaController {

    private final ListarItemsPendientesPort listarItemsPendientesPort;
    private final CamionRepositoryPort camionRepository;
    private final ChoferRepositoryPort choferRepository;
    private final RutaDeRepartoRepositoryPort rutaRepository;
    private final IniciarRutaUseCase iniciarRutaUseCase;
    private final ConfirmarRecepcionUseCase confirmarRecepcionUseCase;
    private final ReportarFallaEntregaUseCase reportarFallaEntregaUseCase;
    private final ProcesarPlanificacionesPendientesUseCase planificacionUseCase;

    public LogisticaController(ListarItemsPendientesPort listarItemsPendientesPort,
            CamionRepositoryPort camionRepository,
            ChoferRepositoryPort choferRepository,
            RutaDeRepartoRepositoryPort rutaRepository,
            IniciarRutaUseCase iniciarRutaUseCase,
            ConfirmarRecepcionUseCase confirmarRecepcionUseCase,
            ReportarFallaEntregaUseCase reportarFallaEntregaUseCase,
            ProcesarPlanificacionesPendientesUseCase planificacionUseCase) {
        this.listarItemsPendientesPort = listarItemsPendientesPort;
        this.camionRepository = camionRepository;
        this.choferRepository = choferRepository;
        this.rutaRepository = rutaRepository;
        this.iniciarRutaUseCase = iniciarRutaUseCase;
        this.confirmarRecepcionUseCase = confirmarRecepcionUseCase;
        this.reportarFallaEntregaUseCase = reportarFallaEntregaUseCase;
        this.planificacionUseCase = planificacionUseCase;
    }

    @GetMapping("/planificacion/pendientes")
    public ResponseEntity<List<ItemPlanificacion>> obtenerTodos() {
        return ResponseEntity.ok(listarItemsPendientesPort.listar());
    }

    // --- CRUD Camiones ---
    @PostMapping("/camiones")
    public ResponseEntity<Void> crearCamion(@RequestBody Camion camion) {
        camionRepository.guardar(camion);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/camiones")
    public ResponseEntity<List<Camion>> listarCamiones() {
        return ResponseEntity.ok(camionRepository.obtenerTodos());
    }

    @PutMapping("/camiones/{patente}")
    public ResponseEntity<Void> actualizarCamion(@PathVariable String patente, @RequestBody Camion camion) {
        camion.setPatente(patente);
        camionRepository.guardar(camion);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/camiones/{patente}")
    public ResponseEntity<Void> eliminarCamion(@PathVariable String patente) {
        camionRepository.eliminar(patente);
        return ResponseEntity.ok().build();
    }

    // --- CRUD Choferes ---
    @PostMapping("/choferes")
    public ResponseEntity<Void> crearChofer(@RequestBody Chofer chofer) {
        choferRepository.guardar(chofer);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/choferes")
    public ResponseEntity<List<Chofer>> listarChoferes() {
        return ResponseEntity.ok(choferRepository.obtenerTodos());
    }

    @DeleteMapping("/choferes/{legajo}")
    public ResponseEntity<Void> eliminarChofer(@PathVariable String legajo) {
        choferRepository.eliminar(legajo);
        return ResponseEntity.ok().build();
    }

    // --- Control de Rutas ---
    @PostMapping("/rutas/{id}/iniciar")
    public ResponseEntity<Void> iniciarRuta(@PathVariable UUID id, @RequestBody IniciarRutaRequest request) {
        iniciarRutaUseCase.iniciarRuta(id, request.getLegajoChofer());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rutas")
    public ResponseEntity<List<RutaDeReparto>> listarRutas() {
        return ResponseEntity.ok(rutaRepository.obtenerTodas());
    }

    @GetMapping("/rutas/{id}")
    public ResponseEntity<RutaDeReparto> obtenerRuta(@PathVariable UUID id) {
        return rutaRepository.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Confirmar y fallar entregas ---
    @PostMapping("/entregas/{idDonacion}/confirmar")
    public ResponseEntity<Void> confirmarEntrega(@PathVariable UUID idDonacion,
            @RequestBody ConfirmarRecepcionRequest request) {
        confirmarRecepcionUseCase.procesar(idDonacion, request.getFotos(), request.getPatenteCamion());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/entregas/{idDonacion}/falla")
    public ResponseEntity<Void> fallarEntrega(@PathVariable UUID idDonacion,
            @RequestBody ReportarFallaRequest request) {
        reportarFallaEntregaUseCase.procesar(idDonacion, request.getMotivo(), request.isPuedeReplanificarse());
        return ResponseEntity.ok().build();
    }

    // --- Planificación ---
    @PostMapping("/planificacion/ejecutar")
    public ResponseEntity<Void> ejecutarPlanificacionManual() {
        planificacionUseCase.procesarPlanificacionesPendientes();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/planificacion/callback")
    public ResponseEntity<Void> procesarCallbackPlanificacion(@RequestBody List<RutaDeReparto> rutas) {
        // Callback dummy para convalidar endpoint externo
        System.out.println("Callback de planificación externa recibido con " + rutas.size() + " rutas.");
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
