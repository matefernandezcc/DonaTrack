package com.donatrack.donaciones.infrastructure.adapters.in.api;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.enums.MedioContacto;
import com.donatrack.donaciones.domain.model.roles.Beneficiario;
import com.donatrack.donaciones.domain.service.matchmaking.MatchmakerService;
import com.donatrack.donaciones.domain.repository.BeneficiarioRepository;
import com.donatrack.donaciones.domain.repository.DonacionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.donatrack.donaciones.application.port.out.NotificacionOutDTO;
import com.donatrack.donaciones.application.port.out.ServicioNotificaciones;
import com.donatrack.donaciones.domain.model.persona.Contacto;
import com.donatrack.donaciones.infrastructure.adapters.out.client.IncentivoClient;

@RestController
@RequestMapping("/donaciones")
public class DonacionController {

    private final MatchmakerService matchmakerService;
    private final ServicioNotificaciones servicioNotificaciones;
    private final IncentivoClient incentivoClient;
    private final DonacionRepository donacionRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final com.donatrack.donaciones.application.service.AsignacionBatchJob asignacionBatchJob;
    
    public DonacionController(MatchmakerService matchmakerService, 
                              ServicioNotificaciones servicioNotificaciones,
                              IncentivoClient incentivoClient,
                              DonacionRepository donacionRepository,
                              BeneficiarioRepository beneficiarioRepository,
                              com.donatrack.donaciones.application.service.AsignacionBatchJob asignacionBatchJob) {
        this.matchmakerService = matchmakerService;
        this.servicioNotificaciones = servicioNotificaciones;
        this.incentivoClient = incentivoClient;
        this.donacionRepository = donacionRepository;
        this.beneficiarioRepository = beneficiarioRepository;
        this.asignacionBatchJob = asignacionBatchJob;
    }

    @PostMapping
    public ResponseEntity<Donacion> crearDonacion(@RequestBody Donacion donacion) {
        // Lógica de guardado (mocked)
        return ResponseEntity.ok(donacion);
    }

    @GetMapping("/{id:[a-fA-F0-9\\-]{36}}")
    public ResponseEntity<Donacion> obtenerDonacion(@PathVariable UUID id) {
        // Lógica de obtención (mocked)
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id:[a-fA-F0-9\\-]{36}}/estado/asignar")
    public ResponseEntity<Void> asignarDonacion(@PathVariable UUID id, @RequestBody Beneficiario beneficiario) {
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
    public ResponseEntity<List<Beneficiario>> sugerirBeneficiarios(@PathVariable UUID id) {
        // En una aplicación real usaríamos donacionRepository.findById(id).orElseThrow(...)
        // Aquí mockeamos la donación hasta tener DB conectada
        Donacion donacionMock = new Donacion(null);
        donacionMock.setId(id);

        List<Beneficiario> disponibles = beneficiarioRepository.buscarTodos();
        List<Beneficiario> sugerencias = matchmakerService.obtenerSugerencias(donacionMock, disponibles);
        
        return ResponseEntity.ok(sugerencias);
    }

    @PutMapping("/{id:[a-fA-F0-9\\-]{36}}")
    public ResponseEntity<Donacion> actualizarDonacion(@PathVariable UUID id, @RequestBody Donacion donacion) {
        // Lógica de actualización (mocked)
        donacion.setId(id);
        return ResponseEntity.ok(donacion);
    }

    @DeleteMapping("/{id:[a-fA-F0-9\\-]{36}}")
    public ResponseEntity<Void> eliminarDonacion(@PathVariable UUID id) {
        // Lógica de eliminación (mocked)
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id:[a-fA-F0-9\\-]{36}}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable UUID id, @RequestBody CambioEstadoRequest request) {
        return donacionRepository.buscarPorId(id).map(donacion -> {
            switch (request.nuevoEstado) {
                case LISTA_PARA_ENTREGAR -> donacion.planificarRuta();
                case EN_TRASLADO -> donacion.iniciarTraslado();
                case ENTREGADA -> donacion.entregar();
                case ENTREGA_FALLIDA -> donacion.fallarEntrega(request.observacion);
                case VENCIDA -> donacion.marcarVencida();
                case EN_DEPOSITO -> donacion.recibirEnDeposito();
                default -> throw new IllegalArgumentException("Estado no soportado vía endpoint genérico");
            }
            
            // La trazabilidad se agrega asumiendo que Admin no está aquí o se extrae del token, pero mockeamos null o agregamos manualmente
            // Los métodos delegan al state y cambian el estado real. El estado hace donacion.cambiarEstado(...) el cual registra.
            // Para ENTREGA_FALLIDA por ejemplo, fallarEntrega recibe justificación.
            
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

class CambioEstadoRequest {
    public com.donatrack.donaciones.domain.model.enums.EstadoDonacionEnum nuevoEstado;
    public String observacion;
    public UUID idUsuario;
}
