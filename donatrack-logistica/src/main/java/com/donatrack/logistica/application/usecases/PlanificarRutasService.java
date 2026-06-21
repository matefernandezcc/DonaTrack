package com.donatrack.logistica.application.usecases;

import com.donatrack.logistica.application.ports.in.PlanificarRutasUseCase;
import com.donatrack.logistica.application.ports.out.CamionRepository;
import com.donatrack.logistica.application.ports.out.PlanificadorRutasExternoPort;
import com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepository;
import com.donatrack.logistica.domain.entities.Camion;
import com.donatrack.logistica.domain.entities.EstadoPlanificacion;
import com.donatrack.logistica.domain.entities.SolicitudPlanificacion;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PlanificarRutasService implements PlanificarRutasUseCase {

    private final SolicitudPlanificacionRepository solicitudRepository;
    private final CamionRepository camionRepository;
    private final PlanificadorRutasExternoPort planificadorExternoPort;

    public PlanificarRutasService(
            SolicitudPlanificacionRepository solicitudRepository,
            CamionRepository camionRepository,
            PlanificadorRutasExternoPort planificadorExternoPort) {
        this.solicitudRepository = solicitudRepository;
        this.camionRepository = camionRepository;
        this.planificadorExternoPort = planificadorExternoPort;
    }

    @Override
    public SolicitudPlanificacion planificar(List<UUID> idsDonaciones) {
        if (idsDonaciones == null || idsDonaciones.isEmpty()) {
            throw new IllegalArgumentException("La lista de IDs de donaciones no puede estar vacía");
        }

        List<Camion> camiones = camionRepository.buscarTodos();

        // Limitación del proveedor: máx 100 donaciones por lote
        // Si hay más de 100, dividimos en lotes
        int batchSize = 100;
        SolicitudPlanificacion primerSolicitud = null;

        for (int i = 0; i < idsDonaciones.size(); i += batchSize) {
            List<UUID> batch = idsDonaciones.subList(i, Math.min(i + batchSize, idsDonaciones.size()));
            
            SolicitudPlanificacion solicitud = new SolicitudPlanificacion(
                UUID.randomUUID(),
                LocalDateTime.now(),
                EstadoPlanificacion.PENDIENTE,
                new ArrayList<>(batch),
                new ArrayList<>()
            );

            solicitudRepository.guardar(solicitud);
            
            if (primerSolicitud == null) {
                primerSolicitud = solicitud;
            }

            // Llamar al proveedor externo asíncronamente
            planificadorExternoPort.solicitarPlanificacion(solicitud.getId(), batch, camiones);
        }

        return primerSolicitud;
    }
}
