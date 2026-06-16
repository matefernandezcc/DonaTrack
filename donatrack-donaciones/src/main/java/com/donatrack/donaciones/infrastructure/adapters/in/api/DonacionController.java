package com.donatrack.donaciones.infrastructure.adapters.in.api;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.roles.Beneficiario;
import com.donatrack.donaciones.domain.service.matchmaking.MatchmakerService;
import com.donatrack.donaciones.domain.repository.BeneficiarioRepository;
import com.donatrack.donaciones.domain.repository.DonacionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.donatrack.donaciones.domain.model.Notificacion;
import com.donatrack.donaciones.domain.model.ServicioNotificaciones;
import com.donatrack.donaciones.domain.model.persona.Contacto;
import com.donatrack.donaciones.domain.enums.MedioContacto;
import com.donatrack.donaciones.infrastructure.adapters.out.client.IncentivoClient;

@RestController
@RequestMapping("/donaciones")
public class DonacionController {

    private final MatchmakerService matchmakerService;
    private final ServicioNotificaciones servicioNotificaciones;
    private final IncentivoClient incentivoClient;
    private final DonacionRepository donacionRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    
    public DonacionController(MatchmakerService matchmakerService, 
                              ServicioNotificaciones servicioNotificaciones,
                              IncentivoClient incentivoClient,
                              DonacionRepository donacionRepository,
                              BeneficiarioRepository beneficiarioRepository) {
        this.matchmakerService = matchmakerService;
        this.servicioNotificaciones = servicioNotificaciones;
        this.incentivoClient = incentivoClient;
        this.donacionRepository = donacionRepository;
        this.beneficiarioRepository = beneficiarioRepository;
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
        servicioNotificaciones.enviar(new Notificacion("Donación asignada", MedioContacto.CORREO), contactoBeneficiario);
        
        // 2. Notificar al donante
        Contacto contactoDonante = new Contacto("donante@test.com", null, null, MedioContacto.CORREO);
        servicioNotificaciones.enviar(new Notificacion("Tu donación ha sido asignada a una entidad", MedioContacto.CORREO), contactoDonante);

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
}
