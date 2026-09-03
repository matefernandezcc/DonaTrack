package com.donatrack.incentivos.infrastructure.adapters.in.scheduler;

import com.donatrack.incentivos.application.ports.out.PerfilDonanteRepository;
import com.donatrack.incentivos.domain.entities.PerfilDonante;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class VerificarInactividadJob {

    private final PerfilDonanteRepository perfilDonanteRepository;

    public VerificarInactividadJob(PerfilDonanteRepository perfilDonanteRepository) {
        this.perfilDonanteRepository = perfilDonanteRepository;
    }

    /**
     * Verifica diariamente (a la medianoche) si algún donante superó
     * los 30 días de inactividad para cortarle la racha.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void ejecutar() {
        List<PerfilDonante> perfiles = perfilDonanteRepository.findAll();
        LocalDate hoy = LocalDate.now();
        
        for (PerfilDonante perfil : perfiles) {
            perfil.procesarInactividad(hoy);
            perfilDonanteRepository.save(perfil);
        }
    }
}
