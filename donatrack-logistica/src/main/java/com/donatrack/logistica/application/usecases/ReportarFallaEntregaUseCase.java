package com.donatrack.logistica.application.usecases;

import com.donatrack.common.events.EntregaNoSatisfactoriaEvent;
import com.donatrack.logistica.application.ports.out.EntregaRepositoryPort;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.domain.entities.Entrega;
import com.donatrack.logistica.domain.entities.Parada;
import com.donatrack.logistica.domain.entities.RutaDeReparto;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ReportarFallaEntregaUseCase {

    private final EntregaRepositoryPort entregaRepository;
    private final RutaDeRepartoRepositoryPort rutaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReportarFallaEntregaUseCase(EntregaRepositoryPort entregaRepository,
                                       RutaDeRepartoRepositoryPort rutaRepository,
                                       ApplicationEventPublisher eventPublisher) {
        this.entregaRepository = entregaRepository;
        this.rutaRepository = rutaRepository;
        this.eventPublisher = eventPublisher;
    }

    public void procesar(UUID idDonacion, String motivo, boolean puedeReplanificarse) {
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

        entrega.marcarNoRecibida();
        entregaRepository.guardar(entrega);

        EntregaNoSatisfactoriaEvent event = new EntregaNoSatisfactoriaEvent(idDonacion, motivo, puedeReplanificarse);
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
