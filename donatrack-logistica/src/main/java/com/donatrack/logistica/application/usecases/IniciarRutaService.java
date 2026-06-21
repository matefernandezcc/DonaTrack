package com.donatrack.logistica.application.usecases;

import com.donatrack.common.events.RutaIniciadaEvent;
import com.donatrack.logistica.application.ports.in.IniciarRutaUseCase;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepository;
import com.donatrack.logistica.domain.entities.Entrega;
import com.donatrack.logistica.domain.entities.Parada;
import com.donatrack.logistica.domain.entities.RutaDeReparto;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class IniciarRutaService implements IniciarRutaUseCase {

    private final RutaDeRepartoRepository rutaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public IniciarRutaService(RutaDeRepartoRepository rutaRepository, ApplicationEventPublisher eventPublisher) {
        this.rutaRepository = rutaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void iniciarRuta(UUID rutaId) {
        RutaDeReparto ruta = rutaRepository.buscarPorId(rutaId)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada"));

        ruta.iniciarRuta();
        rutaRepository.guardar(ruta);

        // Reunir todos los IDs de donaciones
        List<UUID> idsDonaciones = new ArrayList<>();
        if (ruta.getParadas() != null) {
            for (Parada parada : ruta.getParadas()) {
                if (parada.getEntregas() != null) {
                    for (Entrega entrega : parada.getEntregas()) {
                        idsDonaciones.add(entrega.getIdDonacionOriginal());
                    }
                }
            }
        }

        // Publicar evento de integración para los otros servicios
        String patente = ruta.getCamion() != null ? ruta.getCamion().getPatente() : "MOCK-111";
        String chofer = ruta.getChofer() != null ? ruta.getChofer().getNombre() : "Chofer Mock";
        
        eventPublisher.publishEvent(new RutaIniciadaEvent(rutaId, patente, chofer, idsDonaciones));
    }
}
