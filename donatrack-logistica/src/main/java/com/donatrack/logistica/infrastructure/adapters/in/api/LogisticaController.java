package com.donatrack.logistica.infrastructure.adapters.in.api;

import com.donatrack.common.events.PlanificacionProcesadaEvent;
import com.donatrack.logistica.application.ports.in.*;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepository;
import com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepository;
import com.donatrack.logistica.domain.entities.RutaDeReparto;
import com.donatrack.logistica.domain.entities.SolicitudPlanificacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/logistica")
@Tag(name = "Logística", description = "Operaciones de planificación de rutas y trazabilidad de entregas")
public class LogisticaController {

    private final PlanificarRutasUseCase planificarRutasUseCase;
    private final IniciarRutaUseCase iniciarRutaUseCase;
    private final ConfirmarEntregaUseCase confirmarEntregaUseCase;
    private final MarcarEntregaFallidaUseCase marcarEntregaFallidaUseCase;
    
    private final SolicitudPlanificacionRepository solicitudRepository;
    private final RutaDeRepartoRepository rutaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LogisticaController(
            PlanificarRutasUseCase planificarRutasUseCase,
            IniciarRutaUseCase iniciarRutaUseCase,
            ConfirmarEntregaUseCase confirmarEntregaUseCase,
            MarcarEntregaFallidaUseCase marcarEntregaFallidaUseCase,
            SolicitudPlanificacionRepository solicitudRepository,
            RutaDeRepartoRepository rutaRepository,
            ApplicationEventPublisher eventPublisher) {
        this.planificarRutasUseCase = planificarRutasUseCase;
        this.iniciarRutaUseCase = iniciarRutaUseCase;
        this.confirmarEntregaUseCase = confirmarEntregaUseCase;
        this.marcarEntregaFallidaUseCase = marcarEntregaFallidaUseCase;
        this.solicitudRepository = solicitudRepository;
        this.rutaRepository = rutaRepository;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/planificar")
    @Operation(summary = "Solicitar Planificación de Rutas", description = "Inicia el proceso asincrónico para generar planes de rutas dividiendo las donaciones en lotes de hasta 100.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Planificación iniciada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada no válidos")
    })
    public ResponseEntity<SolicitudPlanificacion> planificar(@RequestBody PlanificarRutasRequest request) {
        SolicitudPlanificacion solicitud = planificarRutasUseCase.planificar(request.idsDonaciones());
        return ResponseEntity.ok(solicitud);
    }

    @PostMapping("/callback")
    @Operation(summary = "Callback / Webhook del Planificador Externo", description = "Endpoint de retorno donde el componente externo notifica el resultado de la planificación, actualizando el estado de la solicitud y las entregas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Callback procesado correctamente"),
        @ApiResponse(responseCode = "404", description = "Solicitud de planificación no encontrada")
    })
    public ResponseEntity<Void> callback(@RequestBody PlanificacionCallbackRequestDTO request) {
        SolicitudPlanificacion solicitud = solicitudRepository.buscarPorId(request.solicitudId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitud de planificación no encontrada"));

        solicitud.procesarCallback(request.rutas());
        solicitudRepository.guardar(solicitud);

        if (request.rutas() != null) {
            for (RutaDeReparto ruta : request.rutas()) {
                rutaRepository.guardar(ruta);
            }
        }

        // Publicar evento para actualizar el estado de las donaciones asociadas a LISTA_PARA_ENTREGAR
        eventPublisher.publishEvent(new PlanificacionProcesadaEvent(solicitud.getIdsDonaciones()));

        return ResponseEntity.ok().build();
    }

    @PutMapping("/rutas/{id:[a-fA-F0-9\\-]{36}}/iniciar")
    @Operation(summary = "Iniciar Recorrido de Ruta", description = "Permite a un chofer informar que comienza el recorrido de una ruta, cambiando el estado de las entregas asociadas a EN_TRASLADO y disparando notificaciones en tiempo real.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ruta iniciada correctamente"),
        @ApiResponse(responseCode = "404", description = "Ruta no encontrada")
    })
    public ResponseEntity<Void> iniciarRuta(@PathVariable UUID id) {
        iniciarRutaUseCase.iniciarRuta(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/entregas/{idDonacion:[a-fA-F0-9\\-]{36}}/confirmar")
    @Operation(summary = "Confirmar Recepción de Entrega", description = "Permite a una entidad beneficiaria confirmar la recepción satisfactoria de una donación, registrando fotos y emitiendo el comprobante de entrega.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Recepción confirmada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Entrega no encontrada")
    })
    public ResponseEntity<Void> confirmarEntrega(
            @PathVariable UUID idDonacion,
            @RequestBody ConfirmarEntregaRequest request) {
        confirmarEntregaUseCase.confirmarEntrega(idDonacion, request.fotos(), request.patenteCamion());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/entregas/{idDonacion:[a-fA-F0-9\\-]{36}}/no-recibida")
    @Operation(summary = "Reportar Entrega No Recibida / Fallida", description = "Registra una entrega como fallida, indicando el motivo correspondiente para su posterior revisión administrativa.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Entrega marcada como no recibida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Entrega no encontrada")
    })
    public ResponseEntity<Void> marcarNoRecibida(
            @PathVariable UUID idDonacion,
            @RequestBody EntregaFallidaRequest request) {
        marcarEntregaFallidaUseCase.marcarEntregaFallida(idDonacion, request.motivo());
        return ResponseEntity.ok().build();
    }
}
