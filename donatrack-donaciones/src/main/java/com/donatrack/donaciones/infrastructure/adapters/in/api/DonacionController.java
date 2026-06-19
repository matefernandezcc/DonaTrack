package com.donatrack.donaciones.infrastructure.adapters.in.api;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.enums.MedioContacto;
import com.donatrack.donaciones.domain.model.roles.Beneficiario;
import com.donatrack.donaciones.domain.repository.BeneficiarioRepository;
import com.donatrack.donaciones.domain.repository.DonacionRepository;
import com.donatrack.donaciones.domain.service.MatchmakerService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.donatrack.donaciones.application.port.out.NotificacionOutDTO;
import com.donatrack.donaciones.application.port.out.ServicioNotificaciones;
import com.donatrack.donaciones.domain.model.persona.Contacto;
import com.donatrack.donaciones.infrastructure.adapters.out.client.IncentivoClient;

import com.donatrack.donaciones.application.port.in.RecepcionDonacionesUseCase;
import com.donatrack.donaciones.application.port.in.CargaBienesRequestDTO;
import com.donatrack.donaciones.domain.model.donacion.RecepcionDonacion;

import com.donatrack.donaciones.application.port.in.DonacionResponseDTO;
import com.donatrack.donaciones.application.port.in.DonacionRequestDTO;
import com.donatrack.donaciones.application.port.in.BeneficiarioResponseDTO;
import com.donatrack.donaciones.application.port.in.CambioEstadoRequestDTO;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/donaciones")
public class DonacionController {

    private final MatchmakerService matchmakerService;
    private final ServicioNotificaciones servicioNotificaciones;
    private final IncentivoClient incentivoClient;
    private final DonacionRepository donacionRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final com.donatrack.donaciones.application.service.AsignacionBatchJob asignacionBatchJob;
    private final RecepcionDonacionesUseCase recepcionDonacionesUseCase;
    private final com.donatrack.donaciones.application.service.AuditoriaDepositoJob auditoriaDepositoJob;
    
    public DonacionController(MatchmakerService matchmakerService, 
                              ServicioNotificaciones servicioNotificaciones,
                              IncentivoClient incentivoClient,
                              DonacionRepository donacionRepository,
                              BeneficiarioRepository beneficiarioRepository,
                              com.donatrack.donaciones.application.service.AsignacionBatchJob asignacionBatchJob,
                              RecepcionDonacionesUseCase recepcionDonacionesUseCase,
                              com.donatrack.donaciones.application.service.AuditoriaDepositoJob auditoriaDepositoJob) {
        this.matchmakerService = matchmakerService;
        this.servicioNotificaciones = servicioNotificaciones;
        this.incentivoClient = incentivoClient;
        this.donacionRepository = donacionRepository;
        this.beneficiarioRepository = beneficiarioRepository;
        this.asignacionBatchJob = asignacionBatchJob;
        this.recepcionDonacionesUseCase = recepcionDonacionesUseCase;
        this.auditoriaDepositoJob = auditoriaDepositoJob;
    }

    @PostMapping("/auditoria/vencidos")
    public ResponseEntity<Void> auditarVencidos() {
        auditoriaDepositoJob.auditarVencidos();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recepcion")
    public ResponseEntity<RecepcionDonacion> recibirBienesBrutos(@RequestBody CargaBienesRequestDTO requestDTO) {
        RecepcionDonacion recepcion = recepcionDonacionesUseCase.recibir(requestDTO);
        return ResponseEntity.ok(recepcion);
    }

    @GetMapping("/{id:[a-fA-F0-9\\-]{36}}")
    public ResponseEntity<DonacionResponseDTO> obtenerDonacion(@PathVariable UUID id) {
        // Lógica de obtención (mocked)
        DonacionResponseDTO response = new DonacionResponseDTO(id, null, null);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id:[a-fA-F0-9\\-]{36}}/estado/asignar")
    public ResponseEntity<Void> asignarDonacion(@PathVariable UUID id, @RequestBody BeneficiarioResponseDTO beneficiarioDTO) {
        // Mocking assignment logic
        
        // 1. Notificar a la entidad (beneficiario)
        Contacto contactoBeneficiario = new Contacto("entidad@test.com", null, null, MedioContacto.CORREO);
        servicioNotificaciones.enviar(new NotificacionOutDTO("Donación asignada", MedioContacto.CORREO), contactoBeneficiario);
        
        // 2. Notificar al donante
        Contacto contactoDonante = new Contacto("donante@test.com", null, null, MedioContacto.CORREO);
        servicioNotificaciones.enviar(new NotificacionOutDTO("Tu donación ha sido asignada a una entidad", MedioContacto.CORREO), contactoDonante);

        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id:[a-fA-F0-9\\-]{36}}/estado/entregada")
    public ResponseEntity<Void> donacionEntregada(@PathVariable UUID id, @RequestParam UUID idDonante) {
        // Cuando se entrega y finaliza exitosamente
        // En una app real recuperaríamos la donación de BD para obtener detalles (bienes, entidad)
        
        int cantidadBienesMock = 5;
        List<String> categoriasMock = List.of("Alimentos", "Vestimenta");
        UUID idEntidadMock = UUID.randomUUID(); 
        java.time.LocalDate fechaMock = java.time.LocalDate.now();

        com.donatrack.common.dto.ActividadDonacionDTO dto = new com.donatrack.common.dto.ActividadDonacionDTO(
            idDonante, cantidadBienesMock, categoriasMock, idEntidadMock, fechaMock
        );

        // Informar al modulo de incentivos pasándole el contexto completo
        incentivoClient.registrarActividadDonacionExitosa(idDonante, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id:[a-fA-F0-9\\-]{36}}/matchmaking")
    public ResponseEntity<List<BeneficiarioResponseDTO>> sugerirBeneficiarios(@PathVariable UUID id) {
        // En una aplicación real usaríamos donacionRepository.findById(id).orElseThrow(...)
        // Aquí mockeamos la donación hasta tener DB conectada
        Donacion donacionMock = new Donacion(null);
        donacionMock.setId(id);

        List<Beneficiario> disponibles = beneficiarioRepository.buscarTodos();
        List<Beneficiario> sugerencias = matchmakerService.obtenerSugerencias(donacionMock, disponibles);

        List<BeneficiarioResponseDTO> sugerenciasDTO = sugerencias.stream()
                .map(b -> new BeneficiarioResponseDTO(b.getId()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(sugerenciasDTO);
    }

    @PutMapping("/{id:[a-fA-F0-9\\-]{36}}")
    public ResponseEntity<DonacionResponseDTO> actualizarDonacion(@PathVariable UUID id, @RequestBody DonacionRequestDTO requestDTO) {
        // Lógica de actualización (mocked)
        DonacionResponseDTO response = new DonacionResponseDTO(id, null, null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id:[a-fA-F0-9\\-]{36}}")
    public ResponseEntity<Void> eliminarDonacion(@PathVariable UUID id) {
        // Lógica de eliminación (mocked)
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id:[a-fA-F0-9\\-]{36}}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable UUID id, @RequestBody CambioEstadoRequestDTO request) {
        return donacionRepository.buscarPorId(id).map(donacion -> {
            donacion.cambiarEstado(request.nuevoEstado(), request.observacion(), null);
            donacionRepository.guardar(donacion);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/asignacion-batch")
    public ResponseEntity<Void> ejecutarAsignacionBatch() {
        asignacionBatchJob.asignarDonacionesEnDeposito();
        return ResponseEntity.ok().build();
    }
}
