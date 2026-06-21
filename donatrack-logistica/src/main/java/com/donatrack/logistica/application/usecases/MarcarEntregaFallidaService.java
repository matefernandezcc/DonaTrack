package com.donatrack.logistica.application.usecases;

import com.donatrack.common.events.EntregaFallidaEvent;
import com.donatrack.logistica.application.ports.in.MarcarEntregaFallidaUseCase;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepository;
import com.donatrack.logistica.domain.entities.Entrega;
import com.donatrack.logistica.domain.entities.Parada;
import com.donatrack.logistica.domain.entities.RutaDeReparto;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MarcarEntregaFallidaService implements MarcarEntregaFallidaUseCase {

    private final RutaDeRepartoRepository rutaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MarcarEntregaFallidaService(RutaDeRepartoRepository rutaRepository, ApplicationEventPublisher eventPublisher) {
        this.rutaRepository = rutaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void marcarEntregaFallida(UUID idEntrega, String motivo) {
        RutaDeReparto rutaEncontrada = null;
        Entrega entregaEncontrada = null;

        for (RutaDeReparto ruta : rutaRepository.buscarTodas()) {
            if (ruta.getParadas() != null) {
                for (Parada parada : ruta.getParadas()) {
                    if (parada.getEntregas() != null) {
                        for (Entrega entrega : parada.getEntregas()) {
                            if (entrega.getIdEntrega().equals(idEntrega)) {
                                rutaEncontrada = ruta;
                                entregaEncontrada = entrega;
                                break;
                            }
                        }
                    }
                    if (entregaEncontrada != null) break;
                }
            }
            if (entregaEncontrada != null) break;
        }

        if (entregaEncontrada == null) {
            throw new IllegalArgumentException("Entrega no encontrada en ninguna ruta");
        }

        entregaEncontrada.marcarNoRecibida();
        rutaRepository.guardar(rutaEncontrada);

        // Publicar evento
        eventPublisher.publishEvent(new EntregaFallidaEvent(idEntrega, motivo));
    }
}
