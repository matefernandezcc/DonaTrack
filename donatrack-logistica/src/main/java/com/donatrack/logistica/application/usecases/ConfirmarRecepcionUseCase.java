package com.donatrack.logistica.application.usecases;

import com.donatrack.common.events.EntregaRealizadaEvent;
import com.donatrack.logistica.application.ports.out.EntregaRepositoryPort;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.domain.entities.Entrega;
import com.donatrack.logistica.domain.entities.Parada;
import com.donatrack.logistica.domain.entities.RutaDeReparto;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConfirmarRecepcionUseCase {

    private final EntregaRepositoryPort entregaRepository;
    private final RutaDeRepartoRepositoryPort rutaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ConfirmarRecepcionUseCase(EntregaRepositoryPort entregaRepository,
                                     RutaDeRepartoRepositoryPort rutaRepository,
                                     ApplicationEventPublisher eventPublisher) {
        this.entregaRepository = entregaRepository;
        this.rutaRepository = rutaRepository;
        this.eventPublisher = eventPublisher;
    }

    public void procesar(UUID idDonacion, List<String> fotos, String patenteCamion) {
        Optional<Entrega> entregaOpt = entregaRepository.buscarPorId(idDonacion);
        
        Entrega entrega;
        if (entregaOpt.isPresent()) {
            entrega = entregaOpt.get();
        } else {
            Optional<RutaDeReparto> rutaOpt = rutaRepository.buscarPorIdDonacion(idDonacion);
            if (rutaOpt.isEmpty()) {
                throw new IllegalArgumentException("Entrega no encontrada para idDonacion: " + idDonacion);
            }
            entrega = extraerEntregaDeRuta(rutaOpt.get(), idDonacion);
        }

        // Confirmar recepción en el dominio de Logística
        entrega.confirmarRecepcion(fotos, patenteCamion);
        entregaRepository.guardar(entrega);

        // Publicar evento local
        EntregaRealizadaEvent event = new EntregaRealizadaEvent(
                idDonacion,
                fotos,
                patenteCamion,
                LocalDateTime.now()
        );
        eventPublisher.publishEvent(event);
    }

    private Entrega extraerEntregaDeRuta(RutaDeReparto ruta, UUID idDonacion) {
        for (Parada parada : ruta.getParadas()) {
            if (parada.getEntregas() != null) {
                for (Entrega e : parada.getEntregas()) {
                    if (e.getIdEntrega().equals(idDonacion)) {
                        return e;
                    }
                }
            }
        }
        throw new IllegalStateException("La entrega debería estar en la ruta pero no se encontró");
    }
}
