package com.donatrack.logistica.application.usecases;

import com.donatrack.logistica.application.ports.out.CamionRepositoryPort;
import com.donatrack.logistica.application.ports.out.ChoferRepositoryPort;
import com.donatrack.logistica.application.ports.out.ItemPlanificacionRepositoryPort;
import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepositoryPort;
import com.donatrack.logistica.domain.entities.planificacion.ItemPlanificacion;
import com.donatrack.logistica.domain.entities.planificacion.SolicitudPlanificacion;
import com.donatrack.logistica.domain.entities.reparto.Camion;
import com.donatrack.logistica.domain.entities.reparto.Chofer;
import com.donatrack.logistica.domain.entities.reparto.Direccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanificacionRutasUseCaseTest {

    private ItemPlanificacionRepositoryPort itemPlanificacionRepository;
    private CamionRepositoryPort camionRepository;
    private ChoferRepositoryPort choferRepository;
    private RutaDeRepartoRepositoryPort rutaRepository;
    private SolicitudPlanificacionRepositoryPort solicitudRepository;

    private PlanificacionRutasUseCase useCase;

    @BeforeEach
    void setUp() {
        itemPlanificacionRepository = mock(ItemPlanificacionRepositoryPort.class);
        camionRepository = mock(CamionRepositoryPort.class);
        choferRepository = mock(ChoferRepositoryPort.class);
        rutaRepository = mock(RutaDeRepartoRepositoryPort.class);
        solicitudRepository = mock(SolicitudPlanificacionRepositoryPort.class);

        useCase = new PlanificacionRutasUseCase(
                itemPlanificacionRepository,
                camionRepository,
                choferRepository,
                rutaRepository,
                solicitudRepository
        );
    }

    @Test
    void sin_items_pendientes_no_hace_nada() {
        when(itemPlanificacionRepository.obtenerTodos()).thenReturn(new ArrayList<>());

        useCase.procesarPlanificacionesPendientes();

        verify(camionRepository, never()).obtenerTodos();
        verify(choferRepository, never()).obtenerTodos();
        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    void sin_camiones_o_choferes_no_planifica() {
        List<ItemPlanificacion> items = List.of(crearItem());
        when(itemPlanificacionRepository.obtenerTodos()).thenReturn(items);
        when(camionRepository.obtenerTodos()).thenReturn(new ArrayList<>());
        when(choferRepository.obtenerTodos()).thenReturn(new ArrayList<>());

        useCase.procesarPlanificacionesPendientes();

        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    void respeta_el_limite_de_100_items_por_lote_y_procesa_correctamente() {
        // Preparar 150 ítems (deberían ser 2 lotes: 100 y 50)
        List<ItemPlanificacion> items = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            items.add(crearItem());
        }

        when(itemPlanificacionRepository.obtenerTodos()).thenReturn(items);
        when(camionRepository.obtenerTodos()).thenReturn(List.of(new Camion("ABC1234", 10.0, 2.0, 1000.0)));
        when(choferRepository.obtenerTodos()).thenReturn(List.of(new Chofer("CH1", "Juan")));

        // Ejecutar
        useCase.procesarPlanificacionesPendientes();

        // Verificar que se guardaron exactamente 2 solicitudes
        ArgumentCaptor<SolicitudPlanificacion> solicitudCaptor = ArgumentCaptor.forClass(SolicitudPlanificacion.class);
        verify(solicitudRepository, times(2)).guardar(solicitudCaptor.capture());

        List<SolicitudPlanificacion> solicitudes = solicitudCaptor.getAllValues();
        assertEquals(2, solicitudes.size());
        assertEquals(100, solicitudes.get(0).getIdsDonaciones().size());
        assertEquals(50, solicitudes.get(1).getIdsDonaciones().size());

        // Verificar que se mandó a eliminar a todos los ítems procesados
        verify(itemPlanificacionRepository, times(1)).eliminarTodos(anyList());
    }

    private ItemPlanificacion crearItem() {
        return new ItemPlanificacion(UUID.randomUUID(), 10.0, 1.0, new Direccion("Calle", "123", "Ciudad"));
    }
}
