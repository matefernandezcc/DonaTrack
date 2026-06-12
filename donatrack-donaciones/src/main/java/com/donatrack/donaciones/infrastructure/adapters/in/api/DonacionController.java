package com.donatrack.donaciones.infrastructure.adapters.in.api;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.roles.Beneficiario;
import com.donatrack.donaciones.domain.service.matchmaking.MatchmakerService;
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
    
    public DonacionController(MatchmakerService matchmakerService, 
                              ServicioNotificaciones servicioNotificaciones,
                              IncentivoClient incentivoClient) {
        this.matchmakerService = matchmakerService;
        this.servicioNotificaciones = servicioNotificaciones;
        this.incentivoClient = incentivoClient;
    }

    @PostMapping
    public ResponseEntity<Donacion> crearDonacion(@RequestBody Donacion donacion) {
        // Lógica de guardado (mocked)
        return ResponseEntity.ok(donacion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Donacion> obtenerDonacion(@PathVariable UUID id) {
        // Lógica de obtención (mocked)
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/estado/asignar")
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
    
    @PutMapping("/{id}/estado/entregada")
    public ResponseEntity<Void> donacionEntregada(@PathVariable UUID id, @RequestParam UUID idDonante) {
        // Cuando se entrega y finaliza exitosamente
        // Informar al modulo de incentivos
        incentivoClient.registrarActividadDonacionExitosa(idDonante);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/matchmaking")
    public ResponseEntity<List<Beneficiario>> sugerirBeneficiarios(@PathVariable UUID id) {
        // TODO: obtener donación y beneficiarios disponibles desde repositorios
        return ResponseEntity.ok(List.of());
    }
}
