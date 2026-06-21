package com.donatrack.logistica.infrastructure.adapters.out.client;

import com.donatrack.common.events.PlanificacionProcesadaEvent;
import com.donatrack.logistica.application.ports.out.CamionRepository;
import com.donatrack.logistica.application.ports.out.ChoferRepository;
import com.donatrack.logistica.application.ports.out.PlanificadorRutasExternoPort;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepository;
import com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepository;
import com.donatrack.logistica.domain.entities.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class PlanificadorRutasExternoAdapter implements PlanificadorRutasExternoPort {

    private final CamionRepository camionRepository;
    private final ChoferRepository choferRepository;
    private final SolicitudPlanificacionRepository solicitudRepository;
    private final RutaDeRepartoRepository rutaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PlanificadorRutasExternoAdapter(
            CamionRepository camionRepository,
            ChoferRepository choferRepository,
            SolicitudPlanificacionRepository solicitudRepository,
            RutaDeRepartoRepository rutaRepository,
            ApplicationEventPublisher eventPublisher) {
        this.camionRepository = camionRepository;
        this.choferRepository = choferRepository;
        this.solicitudRepository = solicitudRepository;
        this.rutaRepository = rutaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Async
    public void solicitarPlanificacion(UUID solicitudId, List<UUID> idsDonaciones, List<Camion> camiones) {
        CompletableFuture.runAsync(() -> {
            try {
                // Simular latencia de red de 1 segundo
                Thread.sleep(1000);
                
                // Buscar camión y chofer mock
                Camion camion = camionRepository.buscarTodos().stream().findFirst().orElse(null);
                Chofer chofer = choferRepository.buscarTodos().stream().findFirst().orElse(null);

                // Crear entregas
                List<Entrega> entregas = new ArrayList<>();
                for (UUID idDonacion : idsDonaciones) {
                    entregas.add(new Entrega(idDonacion, EstadoEntrega.PENDIENTE, 10.0, 1.0, null));
                }

                // Parada mock
                Direccion direccionMock = new Direccion("Sarmiento", "1234", "CABA");
                Coordenada coordenadaMock = new Coordenada(-34.6037, -58.3816);
                Parada parada = new Parada(1, direccionMock, coordenadaMock, entregas);

                // Ruta
                RutaDeReparto ruta = new RutaDeReparto(
                    UUID.randomUUID(),
                    LocalDate.now().plusDays(1),
                    false,
                    camion,
                    chofer,
                    Collections.singletonList(parada)
                );

                // Simular el callback llamando al procesamiento interno de la solicitud
                solicitudRepository.buscarPorId(solicitudId).ifPresent(solicitud -> {
                    solicitud.procesarCallback(Collections.singletonList(ruta));
                    solicitudRepository.guardar(solicitud);
                    rutaRepository.guardar(ruta);
                    
                    // Publicar el evento de planificación procesada para actualizar el módulo donaciones
                    eventPublisher.publishEvent(new PlanificacionProcesadaEvent(solicitud.getIdsDonaciones()));
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
