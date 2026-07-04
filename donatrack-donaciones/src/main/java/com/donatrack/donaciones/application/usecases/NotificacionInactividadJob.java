package com.donatrack.donaciones.application.usecases;

import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import com.donatrack.donaciones.domain.entities.roles.Rol;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.application.ports.out.NotificacionOutDTO;
import com.donatrack.donaciones.application.ports.out.ServicioNotificaciones;
import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.domain.entities.enums.MedioContacto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.List;

@Service
public class NotificacionInactividadJob {

    private final PersonaRepository personaRepository;
    private final ServicioNotificaciones servicioNotificaciones;

    public NotificacionInactividadJob(PersonaRepository personaRepository, ServicioNotificaciones servicioNotificaciones) {
        this.personaRepository = personaRepository;
        this.servicioNotificaciones = servicioNotificaciones;
    }

    // Se ejecuta todos los días a las 09:00 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void notificarInactividad() {
        System.out.println("[NotificacionInactividadJob] Iniciando búsqueda de donantes inactivos...");
        List<Persona> personas = personaRepository.obtenerTodas();
        LocalDate hoy = LocalDate.now();

        for (Persona persona : personas) {
            for (Rol rol : persona.getRoles()) {
                if (rol instanceof Donante donante) {
                    if (estaInactivo(donante, hoy)) {
                        Contacto contacto = persona.getContacto();
                        if (contacto != null) {
                            NotificacionOutDTO notificacion = new NotificacionOutDTO(
                                "¡Te extrañamos en DonaTrack! Anímate a realizar una nueva donación para seguir ayudando.",
                                contacto.getMedioPredeterminado() != null ? contacto.getMedioPredeterminado() : MedioContacto.CORREO
                            );
                            servicioNotificaciones.enviar(notificacion, contacto);
                            System.out.println("[NotificacionInactividadJob] Notificación enviada a " + contacto.getCorreoElectronico());
                        }
                    }
                }
            }
        }
    }

    private boolean estaInactivo(Donante donante, LocalDate hoy) {
        List<DonacionOriginal> donaciones = donante.getDonacionesRealizadas();
        if (donaciones == null || donaciones.isEmpty()) {
            return false; // Nunca interactuó
        }

        LocalDate ultimaInteraccion = LocalDate.MIN;

        for (DonacionOriginal d : donaciones) {
            LocalDate fechaRegistro = d.getFechaRecepcion();
            if (fechaRegistro != null && fechaRegistro.isAfter(ultimaInteraccion)) {
                ultimaInteraccion = fechaRegistro;
            }
        }

        long diasInactivo = ChronoUnit.DAYS.between(ultimaInteraccion, hoy);
        return diasInactivo > 20;
    }
}
