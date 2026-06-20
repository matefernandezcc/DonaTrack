package com.donatrack.incentivos.application.usecase;

import com.donatrack.common.dto.ActividadDonacionDTO;
import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.InsigniaObtenidaEvent;
import com.donatrack.incentivos.domain.model.PerfilDonante;
import com.donatrack.incentivos.infrastructure.out.client.NotificacionClient;
import com.donatrack.incentivos.infrastructure.out.client.NotificacionRequest;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

public class RegistrarActividadDonacionUseCase {

    private final NotificacionClient notificacionClient;
    private final ApplicationEventPublisher eventPublisher;

    public RegistrarActividadDonacionUseCase(NotificacionClient notificacionClient,
            ApplicationEventPublisher eventPublisher) {
        this.notificacionClient = notificacionClient;
        this.eventPublisher = eventPublisher;
    }

    public void ejecutar(UUID donanteId, ActividadDonacionDTO actividad) {
        // En un entorno real se trae el PerfilDonante de la base de datos
        // usando un PerfilDonanteRepository
        PerfilDonante perfil = new PerfilDonante(donanteId);

        int insigniasAntes = perfil.getInsigniasObtenidas().size();
        com.donatrack.incentivos.domain.model.categoria.CategoriaDonante categoriaAntes = perfil.getCategoria();

        com.donatrack.incentivos.domain.model.RegistroDonacion donacion = new com.donatrack.incentivos.domain.model.RegistroDonacion(
                actividad.getCantidadBienes(),
                actividad.getCategorias() != null ? new java.util.HashSet<>(actividad.getCategorias()) : null,
                actividad.getIdEntidadBeneficiaria(),
                java.time.YearMonth.from(actividad.getFecha()));

        perfil.registrarDonacionExitosa(donacion);

        int insigniasDespues = perfil.getInsigniasObtenidas().size();
        com.donatrack.incentivos.domain.model.categoria.CategoriaDonante categoriaDespues = perfil.getCategoria();

        boolean misionCompletada = insigniasDespues > insigniasAntes;
        boolean categoriaCambiada = categoriaAntes != categoriaDespues;

        if (misionCompletada) {
            notificacionClient.enviarNotificacion(
                    new NotificacionRequest("donante" + donanteId + "@test.com", "¡Felicidades! Has completado una misión.",
                            "EMAIL"));

            // Publicar el último evento de insignia (la nueva que ganó)
            Insignia ultimaInsignia = perfil.getInsigniasObtenidas().get(insigniasDespues - 1);
            eventPublisher.publishEvent(new InsigniaObtenidaEvent(perfil.getDonanteId(), ultimaInsignia));
        }

        if (categoriaCambiada) {
            notificacionClient.enviarNotificacion(
                    new NotificacionRequest("donante" + donanteId + "@test.com",
                            "¡Increíble! Has subido de categoría a " + categoriaDespues.name(), "EMAIL"));
        }

        // Aquí iría el perfilDonanteRepository.save(perfil);
    }
}
