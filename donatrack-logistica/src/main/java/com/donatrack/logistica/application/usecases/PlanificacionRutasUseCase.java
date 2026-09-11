package com.donatrack.logistica.application.usecases;

import com.donatrack.logistica.application.ports.in.ProcesarPlanificacionesPendientesUseCase;
import com.donatrack.logistica.application.ports.out.CamionRepositoryPort;
import com.donatrack.logistica.application.ports.out.ChoferRepositoryPort;
import com.donatrack.logistica.application.ports.out.ItemPlanificacionRepositoryPort;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.domain.entities.reparto.*;
import com.donatrack.logistica.domain.entities.entregas.*;
import com.donatrack.logistica.domain.entities.planificacion.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class PlanificacionRutasUseCase implements ProcesarPlanificacionesPendientesUseCase {

    private static final int MAX_DONACIONES_POR_LOTE = 100;

    private final ItemPlanificacionRepositoryPort itemPlanificacionRepository;
    private final CamionRepositoryPort camionRepository;
    private final ChoferRepositoryPort choferRepository;
    private final RutaDeRepartoRepositoryPort rutaRepository;
    private final com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepositoryPort solicitudRepository;

    public PlanificacionRutasUseCase(ItemPlanificacionRepositoryPort itemPlanificacionRepository,
                                     CamionRepositoryPort camionRepository,
                                     ChoferRepositoryPort choferRepository,
                                     RutaDeRepartoRepositoryPort rutaRepository,
                                     com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepositoryPort solicitudRepository) {
        this.itemPlanificacionRepository = itemPlanificacionRepository;
        this.camionRepository = camionRepository;
        this.choferRepository = choferRepository;
        this.rutaRepository = rutaRepository;
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    public void procesarPlanificacionesPendientes() {
        List<ItemPlanificacion> itemsPendientes = itemPlanificacionRepository.obtenerTodos();
        if (itemsPendientes.isEmpty()) {
            log.info("No hay ítems de planificación pendientes.");
            return;
        }

        List<Camion> camiones = camionRepository.obtenerTodos();
        List<Chofer> choferes = choferRepository.obtenerTodos();

        if (camiones.isEmpty() || choferes.isEmpty()) {
            log.error("No hay suficientes camiones o choferes para planificar rutas.");
            return;
        }

        int totalItems = itemsPendientes.size();
        log.info("Iniciando procesamiento de {} ítems de planificación", totalItems);

        List<ItemPlanificacion> itemsProcesadosTotal = new ArrayList<>();

        for (int i = 0; i < totalItems; i += MAX_DONACIONES_POR_LOTE) {
            int endIndex = Math.min(i + MAX_DONACIONES_POR_LOTE, totalItems);
            List<ItemPlanificacion> lote = itemsPendientes.subList(i, endIndex);

            log.info("Procesando lote de {} ítems (índice {} al {})", lote.size(), i, endIndex - 1);
            
            // Crear SolicitudPlanificacion para el lote
            SolicitudPlanificacion solicitud = new SolicitudPlanificacion(
                    UUID.randomUUID(),
                    LocalDateTime.now(),
                    EstadoPlanificacion.PENDIENTE,
                    lote.stream().map(ItemPlanificacion::getIdDonacionOriginal).toList()
            );
            solicitudRepository.guardar(solicitud);
            log.info("Generada SolicitudPlanificacion con ID {}", solicitud.getId());

            // Procesar el lote
            procesarLote(lote, camiones, choferes);
            
            itemsProcesadosTotal.addAll(lote);
            
            // TODO: Enviar solicitud al proveedor externo (simulado por ahora)
            log.info("Enviada solicitud {} al proveedor externo.", solicitud.getId());
        }

        // Limpiar los ítems procesados
        itemPlanificacionRepository.eliminarTodos(itemsProcesadosTotal);
        log.info("Se eliminaron {} ítems de planificación procesados.", itemsProcesadosTotal.size());
    }
    
    private void procesarLote(List<ItemPlanificacion> lote, List<Camion> camiones, List<Chofer> choferes) {
        // Algoritmo de asignación (Bin Packing simplificado)
        int camionIndex = 0;
        int choferIndex = 0;

        List<RutaDeReparto> rutasCreadas = new ArrayList<>();
        RutaDeReparto rutaActual = crearNuevaRuta(camiones.get(camionIndex), choferes.get(choferIndex));
        double pesoAcumulado = 0;
        double volumenAcumulado = 0;
        Map<Direccion, Parada> paradasDeRutaActual = new HashMap<>();
        int ordenParada = 1;

        for (ItemPlanificacion item : lote) {
            Camion camionActual = camiones.get(camionIndex);

            // Validar si excede capacidad de peso o volumen del camión actual
            if (pesoAcumulado + item.getPesoEstimado() > camionActual.getCapacidadCarga() ||
                volumenAcumulado + item.getVolumenEstimado() > camionActual.getCapacidadVolumen()) {

                // Guardar ruta actual
                guardarRutaYAsociarParadas(rutaActual, paradasDeRutaActual);
                rutasCreadas.add(rutaActual);

                // Rotar camión y chofer
                camionIndex = (camionIndex + 1) % camiones.size();
                choferIndex = (choferIndex + 1) % choferes.size();

                // Crear nueva ruta
                rutaActual = crearNuevaRuta(camiones.get(camionIndex), choferes.get(choferIndex));
                pesoAcumulado = 0;
                volumenAcumulado = 0;
                paradasDeRutaActual.clear();
                ordenParada = 1;
            }

            // Crear entrega
            Entrega entrega = new Entrega(
                    item.getIdDonacionOriginal(),
                    EstadoEntrega.PENDIENTE,
                    item.getPesoEstimado(),
                    item.getVolumenEstimado(),
                    null
            );

            // Agrupar por dirección de destino
            Direccion destino = item.getDestino();
            Parada parada = paradasDeRutaActual.get(destino);
            if (parada == null) {
                // Generar coordenadas dummy/mock para la parada
                Coordenada coordenada = new Coordenada(-34.6037, -58.3816); // Obelisco Buenos Aires por defecto
                parada = new Parada(ordenParada++, destino, coordenada, new ArrayList<>());
                paradasDeRutaActual.put(destino, parada);
            }
            parada.getEntregas().add(entrega);

            pesoAcumulado += item.getPesoEstimado();
            volumenAcumulado += item.getVolumenEstimado();
        }

        // Guardar la última ruta procesada
        guardarRutaYAsociarParadas(rutaActual, paradasDeRutaActual);
        rutasCreadas.add(rutaActual);

        // En una implementación real, estas rutas se enviarían al proveedor.
        // Aquí solo simulamos la generación.
        for (RutaDeReparto ruta : rutasCreadas) {
            log.info("Ruta planificada (simulada) exitosamente: ID {} con {} paradas.", ruta.getId(), ruta.getParadas().size());
        }
    }

    private RutaDeReparto crearNuevaRuta(Camion camion, Chofer chofer) {
        RutaDeReparto ruta = new RutaDeReparto();
        ruta.setId(UUID.randomUUID());
        ruta.setFechaOperativa(LocalDate.now().plusDays(1)); // Operación al día siguiente
        ruta.setIniciada(false);
        ruta.setCamion(camion);
        ruta.setChofer(chofer);
        ruta.setParadas(new ArrayList<>());
        return ruta;
    }

    private void guardarRutaYAsociarParadas(RutaDeReparto ruta, Map<Direccion, Parada> paradasMap) {
        ruta.setParadas(new ArrayList<>(paradasMap.values()));
    }
}
