package com.donatrack.donaciones.domain.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.donatrack.donaciones.application.ports.out.DonacionOriginalRepository;
import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.enums.MedioContacto;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.persona.PersonaHumana;
import com.donatrack.donaciones.domain.entities.persona.DocumentoIdentidad;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.*;
import com.donatrack.donaciones.domain.entities.enums.TipoDocumento;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.roles.Donante;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GestorEventosLogisticaTest {

    private NotificadorPort mockNotificador;
    private DonacionRepository mockDonacionRepo;
    private PersonaRepository mockPersonaRepo;
    private DonacionOriginalRepository mockDonOrigRepo;
    private GestorEventosLogistica gestor;

    private UUID donacionId;
    private Donacion donacion;

    @BeforeEach
    void setUp() {
        mockNotificador = mock(NotificadorPort.class);
        mockDonacionRepo = mock(DonacionRepository.class);
        mockPersonaRepo = mock(PersonaRepository.class);
        mockDonOrigRepo = mock(DonacionOriginalRepository.class);

        gestor = new GestorEventosLogistica(mockNotificador, mockDonacionRepo, mockPersonaRepo, mockDonOrigRepo);

        Subcategoria sub = new Subcategoria("Ropa", "Ropa");
        donacion = new Donacion(sub);
        donacionId = donacion.getId();
    }

    @Test
    public void testProcesarInicioRutaCambiaEstadoAEnTraslado() {
        when(mockDonacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        UUID rutaId = UUID.randomUUID();
        gestor.procesarInicioRuta(rutaId, List.of(donacionId));

        assertEquals(EstadoDonacion.EN_TRASLADO, donacion.getEstado());
        verify(mockDonacionRepo).guardar(donacion);
    }

    @Test
    public void testProcesarEntregaExitosaCambiaEstadoAEntregada() {
        when(mockDonacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        gestor.procesarEntregaExitosa(donacionId, "COMP-001");

        assertEquals(EstadoDonacion.ENTREGADA, donacion.getEstado());
        verify(mockDonacionRepo).guardar(donacion);
    }

    @Test
    public void testProcesarEntregaFallidaCambiaEstadoAEntregaFallida() {
        when(mockDonacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        gestor.procesarEntregaFallida(donacionId, "Vencimiento");

        assertEquals(EstadoDonacion.ENTREGA_FALLIDA, donacion.getEstado());
        verify(mockDonacionRepo).guardar(donacion);
    }

    @Test
    public void testDonacionInexistenteNoHaceNada() {
        UUID idInexistente = UUID.randomUUID();
        when(mockDonacionRepo.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        // No debería tirar excepción
        assertDoesNotThrow(() -> gestor.procesarInicioRuta(UUID.randomUUID(), List.of(idInexistente)));
        assertDoesNotThrow(() -> gestor.procesarEntregaExitosa(idInexistente, "COMP"));
        assertDoesNotThrow(() -> gestor.procesarEntregaFallida(idInexistente, "motivo"));

        verify(mockDonacionRepo, never()).guardar(any());
    }

    @Test
    public void testProcesarInicioRutaNotificaBeneficiario() {
        Beneficiario beneficiario = new Beneficiario();
        donacion.setEntidadAsignada(beneficiario);

        Contacto contacto = new Contacto("beneficiario@test.com", "111", "222", MedioContacto.CORREO);
        Pais pais = new Pais("Argentina", "Argentino");
        Provincia prov = new Provincia("Buenos Aires", pais);
        Coordenada coord = new Coordenada(-34.0, -58.0);
        Direccion dir = new Direccion("Calle", 1, "CABA", prov, "1000", coord);
        PersonaHumana personaBenef = new PersonaHumana(
                "beneficiario@test.com", contacto, dir,
                new DocumentoIdentidad(TipoDocumento.DNI, "12345678"),
                "Entidad", "Beneficiaria", 0);

        when(mockDonacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));
        when(mockPersonaRepo.buscarPorRolId(beneficiario.getId())).thenReturn(Optional.of(personaBenef));

        gestor.procesarInicioRuta(UUID.randomUUID(), List.of(donacionId));

        verify(mockNotificador).notificar(eq(contacto), anyString(), eq(MedioContacto.CORREO));
    }
}
