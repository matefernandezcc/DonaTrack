package com.donatrack.logistica.application.usecases;

import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepositoryPort;
import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.domain.entities.entregas.EstadoEntrega;
import com.donatrack.logistica.domain.entities.planificacion.EstadoPlanificacion;
import com.donatrack.logistica.domain.entities.planificacion.SolicitudPlanificacion;
import com.donatrack.logistica.domain.entities.reparto.Parada;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcesarCallbackPlanificacionServiceTest {

    private RutaDeRepartoRepositoryPort rutaRepository;
    private SolicitudPlanificacionRepositoryPort solicitudRepository;
    private ProcesarCallbackPlanificacionService service;

    @BeforeEach
    void setUp() {
        rutaRepository = mock(RutaDeRepartoRepositoryPort.class);
        solicitudRepository = mock(SolicitudPlanificacionRepositoryPort.class);
        service = new ProcesarCallbackPlanificacionService(rutaRepository, solicitudRepository);
    }

    @Test
    void procesar_callback_exitoso_actualiza_estados_y_persiste_rutas() {
        UUID idSolicitud = UUID.randomUUID();
        SolicitudPlanificacion solicitud = new SolicitudPlanificacion();
        solicitud.setId(idSolicitud);
        solicitud.setEstado(EstadoPlanificacion.PENDIENTE);

        when(solicitudRepository.buscarPorId(idSolicitud)).thenReturn(Optional.of(solicitud));

        RutaDeReparto ruta = new RutaDeReparto();
        Entrega entrega = new Entrega(UUID.randomUUID(), null, 1.0, 1.0, null);
        Parada parada = new Parada();
        parada.setEntregas(List.of(entrega));
        ruta.setParadas(List.of(parada));

        service.procesarCallback(idSolicitud, List.of(ruta));

        assertEquals(EstadoPlanificacion.PROCESADA, solicitud.getEstado());
        assertEquals(EstadoEntrega.PENDIENTE, entrega.getEstado());
        verify(solicitudRepository).guardar(solicitud);
        verify(rutaRepository).guardar(ruta);
    }

    @Test
    void procesar_callback_solicitud_inexistente_lanza_excepcion() {
        UUID idSolicitud = UUID.randomUUID();
        when(solicitudRepository.buscarPorId(idSolicitud)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.procesarCallback(idSolicitud, List.of()));
        
        verify(rutaRepository, never()).guardar(any());
        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    void procesar_callback_solicitud_ya_procesada_no_hace_nada() {
        UUID idSolicitud = UUID.randomUUID();
        SolicitudPlanificacion solicitud = new SolicitudPlanificacion();
        solicitud.setId(idSolicitud);
        solicitud.setEstado(EstadoPlanificacion.PROCESADA);

        when(solicitudRepository.buscarPorId(idSolicitud)).thenReturn(Optional.of(solicitud));

        service.procesarCallback(idSolicitud, List.of(new RutaDeReparto()));

        verify(rutaRepository, never()).guardar(any());
        verify(solicitudRepository, never()).guardar(any());
    }
}
