package com.donatrack.logistica.application.usecases;

import com.donatrack.common.events.EntregaRealizadaEvent;
import com.donatrack.logistica.application.ports.out.EntregaRepositoryPort;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.domain.entities.reparto.Parada;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProcesarConfirmacionEntregaUseCase {

    private final EntregaRepositoryPort entregaRepository;
    private final RutaDeRepartoRepositoryPort rutaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProcesarConfirmacionEntregaUseCase(EntregaRepositoryPort entregaRepository,
            RutaDeRepartoRepositoryPort rutaRepository,
            ApplicationEventPublisher eventPublisher) {
        this.entregaRepository = entregaRepository;
        this.rutaRepository = rutaRepository;
        this.eventPublisher = eventPublisher;
    }

    public void procesar(UUID idDonacion, List<String> fotos) {
        // En nuestra lógica, idDonacion es igual a idEntrega
        Optional<Entrega> entregaOpt = entregaRepository.buscarPorId(idDonacion);

        Entrega entrega;
        String patenteCamion = "DESCONOCIDO";

        if (entregaOpt.isPresent()) {
            entrega = entregaOpt.get();
        } else {
            // Intentar buscarla dentro de una ruta activa si no está en el repo de entregas
            // sueltas
            Optional<RutaDeReparto> rutaOpt = rutaRepository.buscarPorIdDonacion(idDonacion);
            if (rutaOpt.isEmpty()) {
                throw new IllegalArgumentException("Entrega no encontrada para idDonacion: " + idDonacion);
            }
            RutaDeReparto ruta = rutaOpt.get();
            patenteCamion = ruta.getCamion() != null ? ruta.getCamion().getPatente() : "DESCONOCIDO";

            entrega = extraerEntregaDeRuta(ruta, idDonacion);
        }

        entrega.confirmarRecepcion(fotos, patenteCamion);
        entregaRepository.guardar(entrega);

        // Emitimos el evento que viajará por RabbitMQ hacia Donaciones
        EntregaRealizadaEvent event = new EntregaRealizadaEvent(
                idDonacion,
                entrega.getComprobanteRecepcion().getFotos(),
                entrega.getComprobanteRecepcion().getCamionPatente(),
                entrega.getComprobanteRecepcion().getFechaHora());
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
