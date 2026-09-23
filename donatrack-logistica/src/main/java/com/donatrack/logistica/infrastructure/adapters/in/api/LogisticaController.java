package com.donatrack.logistica.infrastructure.adapters.in.api;

import com.donatrack.logistica.application.ports.in.ListarItemsPendientesPort;
import com.donatrack.logistica.application.ports.in.IniciarRutaUseCase;
import com.donatrack.logistica.application.ports.in.ProcesarCallbackPlanificacionUseCase;
import com.donatrack.logistica.application.ports.in.ProcesarPlanificacionesPendientesUseCase;
import com.donatrack.logistica.application.ports.in.RecepcionarDonacionListaPort;
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
    private final RecepcionarDonacionListaPort recepcionarDonacionListaPort;

    public LogisticaController(ListarItemsPendientesPort listarItemsPendientesPort,
            CamionRepositoryPort camionRepository,
            ChoferRepositoryPort choferRepository,
            RutaDeRepartoRepositoryPort rutaRepository,
            IniciarRutaUseCase iniciarRutaUseCase,
            ConfirmarRecepcionUseCase confirmarRecepcionUseCase,
            ReportarFallaEntregaUseCase reportarFallaEntregaUseCase,
            ProcesarPlanificacionesPendientesUseCase planificacionUseCase,
            ProcesarCallbackPlanificacionUseCase procesarCallbackPlanificacionUseCase,
            RecepcionarDonacionListaPort recepcionarDonacionListaPort) {
        this.listarItemsPendientesPort = listarItemsPendientesPort;
        this.camionRepository = camionRepository;
        this.choferRepository = choferRepository;
        this.rutaRepository = rutaRepository;
        this.iniciarRutaUseCase = iniciarRutaUseCase;
        this.confirmarRecepcionUseCase = confirmarRecepcionUseCase;
        this.reportarFallaEntregaUseCase = reportarFallaEntregaUseCase;
        this.planificacionUseCase = planificacionUseCase;
        this.procesarCallbackPlanificacionUseCase = procesarCallbackPlanificacionUseCase;
        this.recepcionarDonacionListaPort = recepcionarDonacionListaPort;
    }

    @Operation(
        summary = "Listar ítems de planificación pendientes",
        description = "Devuelve todos los ítems de donación recepcionados que están pendientes de ser asignados a una ruta de reparto optimizada.")
    @ApiResponse(responseCode = "200", description = "Lista de ítems pendientes devuelta correctamente")
    @GetMapping("/planificacion/pendientes")
    public ResponseEntity<List<ItemPlanificacion>> obtenerTodos() {
        return ResponseEntity.ok(listarItemsPendientesPort.listar());
    }

    // --- CRUD Camiones ---
    @Operation(
        summary = "Crear camión",
        description = "Registra un nuevo camión en la flota logística con su capacidad de carga y volumen máximo.")
    @ApiResponse(responseCode = "200", description = "Camión registrado exitosamente")
    @PostMapping("/camiones")
    public ResponseEntity<Void> crearCamion(@RequestBody Camion camion) {
        camionRepository.guardar(camion);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Listar camiones",
        description = "Obtiene la flota completa de camiones disponibles y asignados.")
    @ApiResponse(responseCode = "200", description = "Lista de camiones obtenida")
    @GetMapping("/camiones")
    public ResponseEntity<List<Camion>> listarCamiones() {
        return ResponseEntity.ok(camionRepository.obtenerTodos());
    }

    @Operation(
        summary = "Actualizar camión",
        description = "Actualiza la información técnica o de capacidad de un camión existente.")
    @ApiResponse(responseCode = "200", description = "Camión actualizado exitosamente")
    @PutMapping("/camiones/{patente}")
    public ResponseEntity<Void> actualizarCamion(
            @io.swagger.v3.oas.annotations.Parameter(description = "Patente del camión (ej: AA123BB)", example = "AA123BB")
            @PathVariable String patente, 
            @RequestBody Camion camion) {
        camion.setPatente(patente);
        camionRepository.guardar(camion);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Eliminar camión",
        description = "Da de baja un camión de la flota mediante su patente.")
    @ApiResponse(responseCode = "200", description = "Camión dado de baja exitosamente")
    @DeleteMapping("/camiones/{patente}")
    public ResponseEntity<Void> eliminarCamion(
            @io.swagger.v3.oas.annotations.Parameter(description = "Patente del camión a eliminar", example = "AA123BB")
            @PathVariable String patente) {
        camionRepository.eliminar(patente);
        return ResponseEntity.ok().build();
    }

    // --- CRUD Choferes ---
    @Operation(
        summary = "Crear chofer",
        description = "Registra un nuevo chofer con su legajo, nombre, apellido y teléfono de contacto.")
    @ApiResponse(responseCode = "200", description = "Chofer registrado exitosamente")
    @PostMapping("/choferes")
    public ResponseEntity<Void> crearChofer(@RequestBody Chofer chofer) {
        choferRepository.guardar(chofer);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Listar choferes",
        description = "Devuelve todos los choferes habilitados en el sistema logístico.")
    @ApiResponse(responseCode = "200", description = "Lista de choferes obtenida")
    @GetMapping("/choferes")
    public ResponseEntity<List<Chofer>> listarChoferes() {
        return ResponseEntity.ok(choferRepository.obtenerTodos());
    }

    @Operation(
        summary = "Eliminar chofer",
        description = "Da de baja un chofer mediante su número de legajo.")
    @ApiResponse(responseCode = "200", description = "Chofer eliminado exitosamente")
    @DeleteMapping("/choferes/{legajo}")
    public ResponseEntity<Void> eliminarChofer(
            @io.swagger.v3.oas.annotations.Parameter(description = "Número de legajo del chofer", example = "CH-001")
            @PathVariable String legajo) {
        choferRepository.eliminar(legajo);
        return ResponseEntity.ok().build();
    }

    // --- Control de Rutas ---
    @Operation(
        summary = "Iniciar ruta de reparto",
        description = "Cambia el estado de una ruta planificada a 'INICIADA' y asigna el chofer responsable.")
    @ApiResponse(responseCode = "200", description = "Ruta iniciada exitosamente")
    @PostMapping("/rutas/{id}/iniciar")
    public ResponseEntity<Void> iniciarRuta(
            @io.swagger.v3.oas.annotations.Parameter(description = "Identificador único (UUID) de la ruta", example = "31008064-071a-4d7a-ac37-33318f7d9842")
            @PathVariable UUID id, 
            @RequestBody IniciarRutaRequest request) {
        iniciarRutaUseCase.iniciarRuta(id, request.getLegajoChofer());
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Listar rutas de reparto",
        description = "Devuelve todas las rutas generadas con sus paradas, donaciones asociadas y estado.")
    @ApiResponse(responseCode = "200", description = "Lista de rutas obtenida")
    @GetMapping("/rutas")
    public ResponseEntity<List<RutaDeReparto>> listarRutas() {
        return ResponseEntity.ok(rutaRepository.obtenerTodas());
    }

    @Operation(
        summary = "Obtener ruta por ID",
        description = "Devuelve la información detallada de una ruta de reparto específica.")
    @ApiResponse(responseCode = "200", description = "Ruta encontrada")
    @ApiResponse(responseCode = "404", description = "Ruta no encontrada")
    @GetMapping("/rutas/{id}")
    public ResponseEntity<RutaDeReparto> obtenerRuta(
            @io.swagger.v3.oas.annotations.Parameter(description = "UUID de la ruta", example = "31008064-071a-4d7a-ac37-33318f7d9842")
            @PathVariable UUID id) {
        return rutaRepository.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Confirmar y fallar entregas ---
    @Operation(
        summary = "Confirmar entrega en destino",
        description = "Registra que una donación fue entregada con éxito al beneficiario, adjuntando URLs de fotos de comprobante y la patente del camión utilizado.")
    @ApiResponse(responseCode = "200", description = "Entrega confirmada y registrada en el historial")
    @PostMapping("/entregas/{idDonacion}/confirmar")
    public ResponseEntity<Void> confirmarEntrega(
            @io.swagger.v3.oas.annotations.Parameter(description = "UUID de la donación entregada", example = "31008064-071a-4d7a-ac37-33318f7d9841")
            @PathVariable UUID idDonacion,
            @RequestBody ConfirmarRecepcionRequest request) {
        confirmarRecepcionUseCase.procesar(idDonacion, request.getFotos(), request.getPatenteCamion());
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Reportar falla de entrega",
        description = "Registra un intento fallido de entrega (por ejemplo, si el beneficiario no estaba en el domicilio) y determina si puede replanificarse en una próxima ruta.")
    @ApiResponse(responseCode = "200", description = "Falla de entrega registrada")
    @PostMapping("/entregas/{idDonacion}/falla")
    public ResponseEntity<Void> fallarEntrega(
            @io.swagger.v3.oas.annotations.Parameter(description = "UUID de la donación fallida", example = "31008064-071a-4d7a-ac37-33318f7d9841")
            @PathVariable UUID idDonacion,
            @RequestBody ReportarFallaRequest request) {
        reportarFallaEntregaUseCase.procesar(idDonacion, request.getMotivo(), request.isPuedeReplanificarse());
        return ResponseEntity.ok().build();
    }

    // --- Planificación ---
    @Operation(
        summary = "Ejecutar planificación de rutas",
        description = "Toma los ítems pendientes (en lotes de hasta 100) y dispara la solicitud de cálculo de rutas óptimas al optimizador externo.")
    @ApiResponse(responseCode = "200", description = "Planificación iniciada")
    @PostMapping("/planificacion/ejecutar")
    public ResponseEntity<Void> ejecutarPlanificacionManual() {
        planificacionUseCase.procesarPlanificacionesPendientes();
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Callback asíncrono de planificación",
        description = "Endpoint que recibe el webhook del servicio externo de cálculo de rutas una vez finalizada la optimización.")
    @ApiResponse(responseCode = "200", description = "Rutas generadas y guardadas correctamente")
    @PostMapping("/planificacion/callback")
    public ResponseEntity<Void> procesarCallbackPlanificacion(@RequestBody com.donatrack.logistica.infrastructure.adapters.in.api.dto.CallbackPlanificacionRequest request) {
        procesarCallbackPlanificacionUseCase.procesarCallback(request.getIdSolicitud(), request.getRutas());
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Recepcionar donación para entrega",
        description = "Permite que el microservicio de Donaciones o un sistema externo ingrese un ítem de donación con peso, volumen y dirección de destino para ser planificado.")
    @ApiResponse(responseCode = "200", description = "Ítem recepcionado en depósito")
    @PostMapping("/planificacion/items")
    public ResponseEntity<Void> recepcionarDonacionLista(@RequestBody ItemPlanificacionRequest request) {
        ItemPlanificacion item = new ItemPlanificacion(
                request.getIdDonacion(),
                request.getPeso(),
                request.getVolumen(),
                new Direccion(request.getCalleDestino(), request.getAlturaDestino(), request.getLocalidadDestino())
        );
        recepcionarDonacionListaPort.recepcionar(item);
        return ResponseEntity.ok().build();
    }

    // --- DTOs estáticos con esquemas OpenAPI ---
    @lombok.Data
    @lombok.NoArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(description = "Solicitud para iniciar una ruta de reparto")
    public static class IniciarRutaRequest {
        @io.swagger.v3.oas.annotations.media.Schema(description = "Legajo del chofer asignado", example = "CH-001")
        private String legajoChofer;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(description = "Datos de confirmación de recepción en destino")
    public static class ConfirmarRecepcionRequest {
        @io.swagger.v3.oas.annotations.media.Schema(description = "Lista de URLs de fotos comprobantes de la entrega", example = "[\"https://fotos.donatrack.org/remito-123.jpg\"]")
        private List<String> fotos;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Patente del camión que realizó la entrega", example = "AA123BB")
        private String patenteCamion;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(description = "Reporte de incidencia o fallo en la entrega")
    public static class ReportarFallaRequest {
        @io.swagger.v3.oas.annotations.media.Schema(description = "Motivo por el cual no se pudo entregar", example = "El beneficiario no se encontraba en el domicilio")
        private String motivo;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Indica si la entrega debe reintentarse en una ruta posterior", example = "true")
        private boolean puedeReplanificarse;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(description = "Datos de un ítem de donación para planificar")
    public static class ItemPlanificacionRequest {
        @io.swagger.v3.oas.annotations.media.Schema(description = "UUID de la donación asociada", example = "31008064-071a-4d7a-ac37-33318f7d9841")
        private UUID idDonacion;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Peso total en kilogramos", example = "25.5")
        private double peso;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Volumen total en metros cúbicos (m3)", example = "0.5")
        private double volumen;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Calle de destino", example = "Av. Santa Fe")
        private String calleDestino;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Altura o número de puerta", example = "1234")
        private String alturaDestino;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Localidad o ciudad de destino", example = "CABA")
        private String localidadDestino;
    }
}
