package com.donatrack.logistica.application.usecases;

import com.donatrack.logistica.application.ports.in.IniciarRutaUseCase;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.domain.entities.RutaDeReparto;
import com.donatrack.logistica.domain.entities.Parada;
import com.donatrack.logistica.domain.entities.Entrega;
import com.donatrack.common.events.RutaIniciadaEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IniciarRutaService implements IniciarRutaUseCase {

    private final RutaDeRepartoRepositoryPort rutaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public IniciarRutaService(RutaDeRepartoRepositoryPort rutaRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.rutaRepository = rutaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void iniciarRuta(UUID idRuta, String legajoChofer) {
        Optional<RutaDeReparto> rutaOpt = rutaRepository.buscarPorId(idRuta);
        if (rutaOpt.isEmpty()) {
            throw new IllegalArgumentException("La ruta de reparto especificada no existe: " + idRuta);
        }

        RutaDeReparto ruta = rutaOpt.get();
        
        // Validar que el chofer asignado sea quien inicia la ruta
        if (ruta.getChofer() != null && legajoChofer != null && 
            !ruta.getChofer().getLegajo().equalsIgnoreCase(legajoChofer)) {
            throw new IllegalArgumentException("El chofer especificado (" + legajoChofer + ") no es el asignado a la ruta.");
        }

        // Iniciar ruta (cambia estado de entregas a EN_TRASLADO)
        ruta.iniciarRuta();
        rutaRepository.guardar(ruta);

        // Recopilar IDs de donaciones
        List<UUID> idsDonaciones = new ArrayList<>();
        if (ruta.getParadas() != null) {
            for (Parada parada : ruta.getParadas()) {
                if (parada.getEntregas() != null) {
                    for (Entrega entrega : parada.getEntregas()) {
                        idsDonaciones.add(entrega.getIdEntrega());
                    }
                }
            }
        }

        // Publicar evento local
        RutaIniciadaEvent event = new RutaIniciadaEvent(
                ruta.getId(),
                ruta.getCamion() != null ? ruta.getCamion().getPatente() : "",
                ruta.getChofer() != null ? ruta.getChofer().getNombre() : "",
                idsDonaciones
        );
        eventPublisher.publishEvent(event);
        
        System.out.println("Ruta iniciada correctamente y evento publicado para ruta ID: " + idRuta);
    }
}
