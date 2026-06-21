package com.donatrack.logistica.application.usecases;

import com.donatrack.common.events.EntregaConfirmadaEvent;
import com.donatrack.logistica.application.ports.in.ConfirmarEntregaUseCase;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepository;
import com.donatrack.logistica.domain.entities.Entrega;
import com.donatrack.logistica.domain.entities.Parada;
import com.donatrack.logistica.domain.entities.RutaDeReparto;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConfirmarEntregaService implements ConfirmarEntregaUseCase {

    private final RutaDeRepartoRepository rutaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ConfirmarEntregaService(RutaDeRepartoRepository rutaRepository, ApplicationEventPublisher eventPublisher) {
        this.rutaRepository = rutaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void confirmarEntrega(UUID idDonacion, List<String> fotos, String patenteCamion) {
        // Encontrar la ruta que contiene esta donación
        RutaDeReparto rutaEncontrada = null;
        Entrega entregaEncontrada = null;

        for (RutaDeReparto ruta : rutaRepository.buscarTodas()) {
            if (ruta.getParadas() != null) {
                for (Parada parada : ruta.getParadas()) {
                    if (parada.getEntregas() != null) {
                        for (Entrega entrega : parada.getEntregas()) {
                            if (entrega.getIdDonacionOriginal().equals(idDonacion)) {
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
            throw new IllegalArgumentException("Entrega de donación no encontrada en ninguna ruta");
        }

        String patente = (patenteCamion != null && !patenteCamion.isEmpty()) 
            ? patenteCamion 
            : (rutaEncontrada.getCamion() != null ? rutaEncontrada.getCamion().getPatente() : "MOCK-111");

        entregaEncontrada.confirmarRecepcion(fotos, patente);
        rutaRepository.guardar(rutaEncontrada);

        // Publicar evento
        eventPublisher.publishEvent(new EntregaConfirmadaEvent(idDonacion, fotos, patente));
    }
}
