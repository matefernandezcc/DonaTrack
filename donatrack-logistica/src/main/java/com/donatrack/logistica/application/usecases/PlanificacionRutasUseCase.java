package com.donatrack.logistica.application.usecases;

import com.donatrack.logistica.application.ports.in.ProcesarPlanificacionesPendientesUseCase;
import com.donatrack.logistica.application.ports.out.CamionRepositoryPort;
import com.donatrack.logistica.application.ports.out.ChoferRepositoryPort;
import com.donatrack.logistica.application.ports.out.ItemPlanificacionRepositoryPort;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.domain.entities.*;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class PlanificacionRutasUseCase implements ProcesarPlanificacionesPendientesUseCase {

    private final ItemPlanificacionRepositoryPort itemPlanificacionRepository;
    private final CamionRepositoryPort camionRepository;
    private final ChoferRepositoryPort choferRepository;
    private final RutaDeRepartoRepositoryPort rutaRepository;

    public PlanificacionRutasUseCase(ItemPlanificacionRepositoryPort itemPlanificacionRepository,
                                     CamionRepositoryPort camionRepository,
                                     ChoferRepositoryPort choferRepository,
                                     RutaDeRepartoRepositoryPort rutaRepository) {
        this.itemPlanificacionRepository = itemPlanificacionRepository;
        this.camionRepository = camionRepository;
        this.choferRepository = choferRepository;
        this.rutaRepository = rutaRepository;
    }

    @Override
    public void procesarPlanificacionesPendientes() {
        List<ItemPlanificacion> itemsPendientes = itemPlanificacionRepository.obtenerTodos();
        if (itemsPendientes.isEmpty()) {
            System.out.println("No hay ítems de planificación pendientes.");
            return;
        }

        List<Camion> camiones = camionRepository.obtenerTodos();
        List<Chofer> choferes = choferRepository.obtenerTodos();

        if (camiones.isEmpty() || choferes.isEmpty()) {
            System.err.println("No hay suficientes camiones o choferes para planificar rutas.");
            return;
        }

        // Algoritmo de asignación (Bin Packing simplificado)
        int camionIndex = 0;
        int choferIndex = 0;

        List<RutaDeReparto> rutasCreadas = new ArrayList<>();
        RutaDeReparto rutaActual = crearNuevaRuta(camiones.get(camionIndex), choferes.get(choferIndex));
        double pesoAcumulado = 0;
        double volumenAcumulado = 0;
        Map<Direccion, Parada> paradasDeRutaActual = new HashMap<>();
        int ordenParada = 1;

        for (ItemPlanificacion item : itemsPendientes) {
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

        // Guardar todas las rutas y limpiar los ítems pendientes procesados
        for (RutaDeReparto ruta : rutasCreadas) {
            rutaRepository.guardar(ruta);
            System.out.println("Ruta planificada exitosamente: ID " + ruta.getId() + " con " + ruta.getParadas().size() + " paradas.");
        }

        // Limpiar los ítems procesados de la base de datos mock
        itemsPendientes.clear();
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
