package com.donatrack.logistica.application.usecases;

import com.donatrack.logistica.application.ports.out.RutaDeRepartoRepositoryPort;
import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.domain.entities.entregas.EstadoEntrega;
import com.donatrack.logistica.domain.entities.reparto.Camion;
import com.donatrack.logistica.domain.entities.reparto.Chofer;
import com.donatrack.logistica.domain.entities.reparto.Parada;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IniciarRutaServiceTest {

    private RutaDeRepartoRepositoryPort rutaRepository;
    private ApplicationEventPublisher eventPublisher;
    private IniciarRutaService service;

    @BeforeEach
    void setUp() {
        rutaRepository = mock(RutaDeRepartoRepositoryPort.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new IniciarRutaService(rutaRepository, eventPublisher);
    }

    @Test
    void iniciar_ruta_existente_y_correcta_marca_como_iniciada_y_publica_evento() {
        UUID idRuta = UUID.randomUUID();
        RutaDeReparto ruta = crearRuta(idRuta, "CH1");
        
        when(rutaRepository.buscarPorId(idRuta)).thenReturn(Optional.of(ruta));

        service.iniciarRuta(idRuta, "CH1");

        assertTrue(ruta.getIniciada());
        assertEquals(EstadoEntrega.EN_TRASLADO, ruta.getParadas().get(0).getEntregas().get(0).getEstado());
        verify(rutaRepository).guardar(ruta);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void ruta_inexistente_lanza_excepcion() {
        UUID idRuta = UUID.randomUUID();
        when(rutaRepository.buscarPorId(idRuta)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.iniciarRuta(idRuta, "CH1"));
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void chofer_incorrecto_lanza_excepcion() {
        UUID idRuta = UUID.randomUUID();
        RutaDeReparto ruta = crearRuta(idRuta, "CH1");
        
        when(rutaRepository.buscarPorId(idRuta)).thenReturn(Optional.of(ruta));

        assertThrows(IllegalArgumentException.class, () -> service.iniciarRuta(idRuta, "OTRO"));
        assertFalse(ruta.getIniciada());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    private RutaDeReparto crearRuta(UUID idRuta, String legajoChofer) {
        RutaDeReparto ruta = new RutaDeReparto();
        ruta.setId(idRuta);
        ruta.setChofer(new Chofer(legajoChofer, "Juan"));
        ruta.setCamion(new Camion("ABC", 10.0, 2.0, 1000.0));
        ruta.setIniciada(false);
        
        Entrega entrega = new Entrega(UUID.randomUUID(), EstadoEntrega.PENDIENTE, 1.0, 1.0, null);
        Parada parada = new Parada();
        parada.setEntregas(List.of(entrega));
        ruta.setParadas(List.of(parada));
        
        return ruta;
    }
}
