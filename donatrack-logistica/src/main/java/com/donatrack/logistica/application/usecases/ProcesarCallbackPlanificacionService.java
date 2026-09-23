package com.donatrack.logistica.application.usecases;

import com.donatrack.logistica.application.ports.in.ProcesarCallbackPlanificacionUseCase;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepositoryPort;
import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.domain.entities.entregas.EstadoEntrega;
import com.donatrack.logistica.domain.entities.planificacion.EstadoPlanificacion;
import com.donatrack.logistica.domain.entities.planificacion.SolicitudPlanificacion;
import com.donatrack.logistica.domain.entities.reparto.Parada;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ProcesarCallbackPlanificacionService implements ProcesarCallbackPlanificacionUseCase {

    private final RutaDeRepartoRepositoryPort rutaRepository;
    private final SolicitudPlanificacionRepositoryPort solicitudRepository;

    public ProcesarCallbackPlanificacionService(RutaDeRepartoRepositoryPort rutaRepository,
                                                SolicitudPlanificacionRepositoryPort solicitudRepository) {
        this.rutaRepository = rutaRepository;
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    public void procesarCallback(UUID idSolicitud, List<RutaDeReparto> rutas) {
        log.info("Recibido callback de planificación para la solicitud {} con {} rutas", idSolicitud, rutas.size());

        SolicitudPlanificacion solicitud = solicitudRepository.buscarPorId(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + idSolicitud));

        if (solicitud.getEstado() == EstadoPlanificacion.PROCESADA) {
            log.warn("La solicitud {} ya se encuentra procesada.", idSolicitud);
            return;
        }

        // Persistir cada ruta y actualizar estado de entregas a PENDIENTE
        for (RutaDeReparto ruta : rutas) {
            // Asegurarnos de que las entregas están inicializadas correctamente como pendientes
            if (ruta.getParadas() != null) {
                for (Parada parada : ruta.getParadas()) {
                    if (parada.getEntregas() != null) {
                        for (Entrega entrega : parada.getEntregas()) {
                            if (entrega.getEstado() == null) {
                                entrega.setEstado(EstadoEntrega.PENDIENTE);
                            }
                        }
                    }
                }
            }
            rutaRepository.guardar(ruta);
            log.info("Ruta {} persistida", ruta.getId());
        }

        // Actualizar el estado de la solicitud
        solicitud.setEstado(EstadoPlanificacion.PROCESADA);
        solicitudRepository.guardar(solicitud);
        
        log.info("Solicitud {} marcada como PROCESADA", idSolicitud);
    }
}
