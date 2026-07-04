package com.donatrack.donaciones.infrastructure.adapters.in.api;

import com.donatrack.donaciones.application.ports.in.RecepcionDonacionesUseCase;
import com.donatrack.donaciones.application.ports.in.CargaBienesRequestDTO;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;

import com.donatrack.donaciones.application.ports.in.DonacionResponseDTO;
import com.donatrack.donaciones.application.ports.in.DonacionRequestDTO;
import com.donatrack.donaciones.application.ports.in.CambioEstadoRequestDTO;
import com.donatrack.donaciones.application.ports.in.BeneficiarioResponseDTO;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.enums.MedioContacto;
import com.donatrack.donaciones.application.ports.out.NotificacionOutDTO;

import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.application.ports.out.BeneficiarioRepository;
import com.donatrack.donaciones.application.ports.out.ServicioNotificaciones;
import com.donatrack.donaciones.infrastructure.adapters.out.client.IncentivoClient;
import com.donatrack.donaciones.domain.services.MatchmakerService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DonacionController {

    private final MatchmakerService matchmakerService;
    private final ServicioNotificaciones servicioNotificaciones;
    private final IncentivoClient incentivoClient;
    private final DonacionRepository donacionRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final com.donatrack.donaciones.application.usecases.AsignacionBatchJob asignacionBatchJob;
    private final RecepcionDonacionesUseCase recepcionDonacionesUseCase;
    private final com.donatrack.donaciones.application.usecases.AuditoriaDepositoJob auditoriaDepositoJob;

    public DonacionController(MatchmakerService matchmakerService,
            ServicioNotificaciones servicioNotificaciones,
            IncentivoClient incentivoClient,
            DonacionRepository donacionRepository,
            BeneficiarioRepository beneficiarioRepository,
            com.donatrack.donaciones.application.usecases.AsignacionBatchJob asignacionBatchJob,
            RecepcionDonacionesUseCase recepcionDonacionesUseCase,
            com.donatrack.donaciones.application.usecases.AuditoriaDepositoJob auditoriaDepositoJob) {
        this.matchmakerService = matchmakerService;
        this.servicioNotificaciones = servicioNotificaciones;
        this.incentivoClient = incentivoClient;
        this.donacionRepository = donacionRepository;
        this.beneficiarioRepository = beneficiarioRepository;
        this.asignacionBatchJob = asignacionBatchJob;
        this.recepcionDonacionesUseCase = recepcionDonacionesUseCase;
        this.auditoriaDepositoJob = auditoriaDepositoJob;
    }

    @PostMapping("/donaciones/auditoria/vencidos")
    public ResponseEntity<Void> auditarVencidos() {
        auditoriaDepositoJob.auditarVencidos();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recepciones")
    public ResponseEntity<DonacionOriginal> recibirBienesBrutos(@RequestBody CargaBienesRequestDTO requestDTO) {
        DonacionOriginal recepcion = recepcionDonacionesUseCase.recibir(requestDTO);
        return ResponseEntity.ok(recepcion);
    }

    @GetMapping("/donaciones/{id:[a-fA-F0-9\\-]{36}}")
    public ResponseEntity<DonacionResponseDTO> obtenerDonacion(@PathVariable UUID id) {
        return donacionRepository.buscarPorId(id).map(donacion -> {
            DonacionResponseDTO response = new DonacionResponseDTO(
                    donacion.getId(),
                    donacion.getEstado(),
                    donacion.getEntidadAsignada() != null ? donacion.getEntidadAsignada().getId() : null
            );
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            DonacionResponseDTO response = new DonacionResponseDTO(id, null, null);
            return ResponseEntity.ok(response);
        });
    }

    @PutMapping("/donaciones/{id:[a-fA-F0-9\\-]{36}}/estado/en_deposito")
    public ResponseEntity<Void> donacionEnDeposito(@PathVariable UUID id) {
        Donacion donacion = donacionRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Donacion no encontrada"));

        UUID idDonante = UUID.randomUUID(); // idDonante mockeado

        int cantidadBienes = donacion.getBienes().size();
        List<String> categorias = donacion.getCategoriasString();
        UUID idEntidadBeneficiaria = null;
        LocalDate fecha = LocalDate.now();

        com.donatrack.common.dto.ActividadDonacionDTO dto = new com.donatrack.common.dto.ActividadDonacionDTO(
                id, idDonante, cantidadBienes, categorias, idEntidadBeneficiaria, fecha);

        incentivoClient.registrarActividadDonacionEnDeposito(id, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/donaciones/{id:[a-fA-F0-9\\-]{36}}/estado/asignar")
    public ResponseEntity<Void> asignarDonacion(@PathVariable UUID id,
            @RequestBody BeneficiarioResponseDTO beneficiarioDTO) {
        Contacto contactoBeneficiario = new Contacto("entidad@test.com", null, null, MedioContacto.CORREO);
        servicioNotificaciones.enviar(new NotificacionOutDTO("Donación asignada", MedioContacto.CORREO),
                contactoBeneficiario);

        Contacto contactoDonante = new Contacto("donante@test.com", null, null, MedioContacto.CORREO);
        servicioNotificaciones.enviar(
                new NotificacionOutDTO("Tu donación ha sido asignada a una entidad", MedioContacto.CORREO),
                contactoDonante);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/donaciones/{id:[a-fA-F0-9\\-]{36}}/estado/entregada")
    public ResponseEntity<Void> donacionEntregada(@PathVariable UUID id, @RequestParam UUID idDonante) {
        int cantidadBienesMock = 5;
        List<String> categoriasMock = List.of("Alimentos", "Vestimenta");
        UUID idEntidadMock = UUID.randomUUID();
        java.time.LocalDate fechaMock = java.time.LocalDate.now();

        com.donatrack.common.dto.ActividadDonacionDTO dto = new com.donatrack.common.dto.ActividadDonacionDTO(
                id, idDonante, cantidadBienesMock, categoriasMock, idEntidadMock, fechaMock);

        incentivoClient.registrarActividadDonacionExitosa(idDonante, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/donaciones/{id:[a-fA-F0-9\\-]{36}}/matchmaking")
    public ResponseEntity<List<BeneficiarioResponseDTO>> sugerirBeneficiarios(@PathVariable UUID id) {
        Donacion donacionMock = new Donacion(null);
        donacionMock.setId(id);

        List<Beneficiario> disponibles = beneficiarioRepository.buscarTodos();
        List<Beneficiario> sugerencias = matchmakerService.obtenerSugerencias(donacionMock, disponibles);

        List<BeneficiarioResponseDTO> sugerenciasDTO = sugerencias.stream()
                .map(b -> new BeneficiarioResponseDTO(b.getId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(sugerenciasDTO);
    }

    @PutMapping("/donaciones/{id:[a-fA-F0-9\\-]{36}}")
    public ResponseEntity<DonacionResponseDTO> actualizarDonacion(@PathVariable UUID id,
            @RequestBody DonacionRequestDTO requestDTO) {
        DonacionResponseDTO response = new DonacionResponseDTO(id, null, null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/donaciones/{id:[a-fA-F0-9\\-]{36}}")
    public ResponseEntity<Void> eliminarDonacion(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/donaciones/{id:[a-fA-F0-9\\-]{36}}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable UUID id, @RequestBody CambioEstadoRequestDTO request) {
        return donacionRepository.buscarPorId(id).map(donacion -> {
            donacion.cambiarEstado(request.nuevoEstado(), request.observacion(), null);
            donacionRepository.guardar(donacion);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/donaciones/asignacion-batch")
    public ResponseEntity<Void> ejecutarAsignacionBatch() {
        asignacionBatchJob.asignarDonacionesEnDeposito();
        return ResponseEntity.ok().build();
    }
}
