package com.donatrack.donaciones.infrastructure.adapters.in.scheduler;

import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.necesidades.Necesidad;
import com.donatrack.donaciones.domain.entities.necesidades.NecesidadRecurrente;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class RenovacionPeriodosJob {

    private final PersonaRepository personaRepository;

    public RenovacionPeriodosJob(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Ejecuta a la medianoche todos los días
    public void renovarPeriodosVencidos() {
        List<Persona> todasLasPersonas = personaRepository.obtenerTodas();
        LocalDate hoy = LocalDate.now();
        boolean huboCambios = false;

        for (Persona persona : todasLasPersonas) {
            Beneficiario beneficiario = (Beneficiario) persona.getRoles().stream()
                    .filter(rol -> rol instanceof Beneficiario)
                    .findFirst()
                    .orElse(null);

            if (beneficiario == null) continue;

            for (Necesidad necesidad : beneficiario.getNecesidadesDeclaradas()) {
                if (necesidad instanceof NecesidadRecurrente recurrente) {
                    if (Boolean.TRUE.equals(recurrente.getActiva()) && 
                        recurrente.getPeriodoActual() != null &&
                        !recurrente.getPeriodoActual().getFechaFin().isAfter(hoy)) {
                        
                        recurrente.cerrarPeriodoYCrearSiguiente();
                        huboCambios = true;
                        System.out.println("Período renovado para NecesidadRecurrente ID: " + recurrente.getId());
                    }
                }
            }

            if (huboCambios) {
                personaRepository.guardar(persona);
                huboCambios = false; // Resetear para la siguiente persona
            }
        }
    }
}
