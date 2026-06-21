package com.donatrack.incentivos.application.usecases;

import com.donatrack.common.dto.ActividadDonacionDTO;
import com.donatrack.incentivos.domain.entities.Insignia;
import com.donatrack.incentivos.domain.entities.InsigniaObtenidaEvent;
import com.donatrack.incentivos.domain.entities.PerfilDonante;
import com.donatrack.incentivos.application.ports.out.IncentivosNotificacionPort;
import com.donatrack.incentivos.application.ports.out.NotificacionRequest;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

public class RegistrarActividadDonacionUseCase {

    private final IncentivosNotificacionPort notificacionPort;
    private final ApplicationEventPublisher eventPublisher;

    public RegistrarActividadDonacionUseCase(IncentivosNotificacionPort notificacionPort,
            ApplicationEventPublisher eventPublisher) {
        this.notificacionPort = notificacionPort;
        this.eventPublisher = eventPublisher;
    }

    public void ejecutar(UUID donanteId, ActividadDonacionDTO actividad) {
        // En un entorno real se trae el PerfilDonante de la base de datos
        // usando un PerfilDonanteRepository
        PerfilDonante perfil = new PerfilDonante(donanteId);

        int insigniasAntes = perfil.getInsigniasObtenidas().size();
        com.donatrack.incentivos.domain.entities.categoria.CategoriaDonante categoriaAntes = perfil.getCategoria();

        com.donatrack.incentivos.domain.entities.RegistroDonacion donacion = new com.donatrack.incentivos.domain.entities.RegistroDonacion(
                actividad.getCantidadBienes(),
                actividad.getCategorias() != null ? new java.util.HashSet<>(actividad.getCategorias()) : null,
                actividad.getIdEntidadBeneficiaria(),
                java.time.YearMonth.from(actividad.getFecha()));

        perfil.registrarDonacionExitosa(donacion);

        int insigniasDespues = perfil.getInsigniasObtenidas().size();
        com.donatrack.incentivos.domain.entities.categoria.CategoriaDonante categoriaDespues = perfil.getCategoria();

        boolean misionCompletada = insigniasDespues > insigniasAntes;
        boolean categoriaCambiada = categoriaAntes != categoriaDespues;

        if (misionCompletada) {
            notificacionPort.enviarNotificacion(
                    new NotificacionRequest("donante" + donanteId + "@test.com", "¡Felicidades! Has completado una misión.",
                            "EMAIL"));

            // Publicar el último evento de insignia (la nueva que ganó)
            Insignia ultimaInsignia = perfil.getInsigniasObtenidas().get(insigniasDespues - 1);
            eventPublisher.publishEvent(new InsigniaObtenidaEvent(perfil.getDonanteId(), ultimaInsignia));
        }

        if (categoriaCambiada) {
            notificacionPort.enviarNotificacion(
                    new NotificacionRequest("donante" + donanteId + "@test.com",
                            "¡Increíble! Has subido de categoría a " + categoriaDespues.name(), "EMAIL"));
        }

        // Aquí iría el perfilDonanteRepository.save(perfil);
    }
}
