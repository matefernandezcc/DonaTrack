package com.donatrack.donaciones.application.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.donatrack.common.events.DonacionReplanificadaEvent;
import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

public class ReplanificarEntregaUseCaseTest {

    private DonacionRepository donacionRepo;
    private ApplicationEventPublisher eventPublisher;
    private ReplanificarEntregaUseCase useCase;

    private Donacion donacion;
    private UUID donacionId;

    @BeforeEach
    void setUp() {
        donacionRepo = mock(DonacionRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        useCase = new ReplanificarEntregaUseCase(donacionRepo, eventPublisher);

        Subcategoria sub = new Subcategoria("Alimentos", "Alimentos no perecederos");
        donacion = new Donacion(sub);
        donacionId = donacion.getId();
    }

    @Test
    public void replanificaDonacionEnEntregaFallida() {
        donacion.cambiarEstado(EstadoDonacion.ENTREGA_FALLIDA, "Falló la entrega", null);
        when(donacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        useCase.replanificar(donacionId, "admin-001");

        assertEquals(EstadoDonacion.ASIGNADA, donacion.getEstado());
        verify(donacionRepo).guardar(donacion);
    }

    @Test
    public void publicaEventoDeReplanificacion() {
        donacion.cambiarEstado(EstadoDonacion.ENTREGA_FALLIDA, "Falló la entrega", null);
        when(donacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        useCase.replanificar(donacionId, "admin-001");

        ArgumentCaptor<DonacionReplanificadaEvent> captor = ArgumentCaptor.forClass(DonacionReplanificadaEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        DonacionReplanificadaEvent event = captor.getValue();
        assertEquals(donacionId, event.getIdDonacion());
    }

    @Test
    public void noReplanificaSiEstadoNoEsEntregaFallida() {
        // Donación en estado EN_DEPOSITO (estado inicial)
        when(donacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        assertThrows(IllegalStateException.class, () -> useCase.replanificar(donacionId, "admin-001"));
        verify(donacionRepo, never()).guardar(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    public void noReplanificaSiEstadoEsEnTraslado() {
        donacion.cambiarEstado(EstadoDonacion.EN_TRASLADO, "En camino", null);
        when(donacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        assertThrows(IllegalStateException.class, () -> useCase.replanificar(donacionId, "admin-001"));
    }

    @Test
    public void lanzaExcepcionSiDonacionNoExiste() {
        UUID idInexistente = UUID.randomUUID();
        when(donacionRepo.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase.replanificar(idInexistente, "admin-001"));
        verify(donacionRepo, never()).guardar(any());
    }
}
