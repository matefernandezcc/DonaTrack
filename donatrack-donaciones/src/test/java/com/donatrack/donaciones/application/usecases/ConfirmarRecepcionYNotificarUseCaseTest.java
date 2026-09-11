package com.donatrack.donaciones.application.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.donatrack.common.events.EntregaRealizadaEvent;
import com.donatrack.common.events.NotificacionEntregaExitosaEvent;
import com.donatrack.common.events.NotificacionInicioRutaEvent;
import com.donatrack.donaciones.application.ports.out.DonacionOriginalRepository;
import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.enums.MedioContacto;
import com.donatrack.donaciones.domain.entities.enums.TipoDocumento;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.DocumentoIdentidad;
import com.donatrack.donaciones.domain.entities.persona.PersonaHumana;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.*;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.roles.Donante;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ConfirmarRecepcionYNotificarUseCaseTest {

    private DonacionRepository donacionRepo;
    private DonacionOriginalRepository donacionOriginalRepo;
    private PersonaRepository personaRepo;
    private ApplicationEventPublisher eventPublisher;
    private ConfirmarRecepcionYNotificarUseCase useCase;

    private Donacion donacion;
    private UUID donacionId;
    private Beneficiario beneficiario;
    private Donante donante;

    @BeforeEach
    void setUp() {
        donacionRepo = mock(DonacionRepository.class);
        donacionOriginalRepo = mock(DonacionOriginalRepository.class);
        personaRepo = mock(PersonaRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        useCase = new ConfirmarRecepcionYNotificarUseCase(donacionRepo, donacionOriginalRepo, personaRepo, eventPublisher);

        Subcategoria sub = new Subcategoria("Alimentos", "Alimentos no perecederos");
        donacion = new Donacion(sub);
        donacionId = donacion.getId();

        beneficiario = new Beneficiario();
        donante = new Donante();
    }

    @Test
    public void confirmaRecepcionYPublicaEvento() {
        // El beneficiario tiene la donación en sus asignadas para que confirmarRecepcion retorne true
        beneficiario.getDonacionesAsignadas().add(donacion);
        donacion.setEntidadAsignada(beneficiario);

        // Configurar donante
        DonacionOriginal donOriginal = new DonacionOriginal("Desc", donante, "user1");
        when(donacionOriginalRepo.buscarPorIdDonacion(donacionId)).thenReturn(Optional.of(donOriginal));

        Contacto contactoDonante = new Contacto("donante@test.com", "1111", "2222", MedioContacto.CORREO);
        PersonaHumana personaDonante = crearPersona("donante@test.com", contactoDonante, "Juan", "Pérez");
        when(personaRepo.buscarPorRolId(donante.getId())).thenReturn(Optional.of(personaDonante));

        // Configurar beneficiario contacto
        Contacto contactoBenef = new Contacto("entidad@test.com", "3333", "4444", MedioContacto.CORREO);
        PersonaHumana personaBenef = crearPersona("entidad@test.com", contactoBenef, "Entidad", "ONG");
        when(personaRepo.buscarPorRolId(beneficiario.getId())).thenReturn(Optional.of(personaBenef));

        when(donacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        LocalDateTime ahora = LocalDateTime.now();
        EntregaRealizadaEvent entregaEvent = new EntregaRealizadaEvent(
                donacionId, List.of("http://foto1.jpg", "http://foto2.jpg"), "AA-123-BB", ahora);

        useCase.procesar(entregaEvent);

        // Verificar que la donación fue confirmada (cambiada a ENTREGADA)
        assertEquals(EstadoDonacion.ENTREGADA, donacion.getEstado());
        verify(donacionRepo).guardar(donacion);

        // Verificar que se publicó el evento de notificación
        ArgumentCaptor<NotificacionEntregaExitosaEvent> captor = ArgumentCaptor.forClass(NotificacionEntregaExitosaEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        NotificacionEntregaExitosaEvent notifEvent = captor.getValue();
        assertEquals(donacionId, notifEvent.getIdDonacion());
        assertEquals("AA-123-BB", notifEvent.getPatenteCamion());
        assertEquals(ahora, notifEvent.getFechaHora());
        assertEquals(2, notifEvent.getEnlacesFotos().size());
    }

    @Test
    public void donacionInexistenteNoHaceNada() {
        UUID idInexistente = UUID.randomUUID();
        when(donacionRepo.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        EntregaRealizadaEvent event = new EntregaRealizadaEvent(
                idInexistente, List.of(), "AA-123-BB", LocalDateTime.now());

        assertDoesNotThrow(() -> useCase.procesar(event));
        verify(donacionRepo, never()).guardar(any());
        verify(eventPublisher, never()).publishEvent(any(NotificacionEntregaExitosaEvent.class));
    }

    @Test
    public void sinBeneficiarioNoConfirmaPeroSiPublicaEvento() {
        donacion.setEntidadAsignada(null);
        when(donacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        EntregaRealizadaEvent event = new EntregaRealizadaEvent(
                donacionId, List.of("http://foto1.jpg"), "BB-456-CC", LocalDateTime.now());

        useCase.procesar(event);

        // Sin beneficiario, no se llama a confirmarRecepcion, estado no cambia
        assertNotEquals(EstadoDonacion.ENTREGADA, donacion.getEstado());
        // Pero sí se publica el evento de notificación
        verify(eventPublisher).publishEvent(any(NotificacionEntregaExitosaEvent.class));
    }

    @Test
    public void eventoContieneContactosDelDonanteYEntidad() {
        beneficiario.getDonacionesAsignadas().add(donacion);
        donacion.setEntidadAsignada(beneficiario);

        DonacionOriginal donOriginal = new DonacionOriginal("Desc", donante, "user1");
        when(donacionOriginalRepo.buscarPorIdDonacion(donacionId)).thenReturn(Optional.of(donOriginal));

        Contacto contactoDonante = new Contacto("donante@email.com", null, null, MedioContacto.CORREO);
        PersonaHumana personaDonante = crearPersona("donante@email.com", contactoDonante, "María", "López");
        when(personaRepo.buscarPorRolId(donante.getId())).thenReturn(Optional.of(personaDonante));

        Contacto contactoBenef = new Contacto(null, "1155550000", null, MedioContacto.TELEFONO);
        PersonaHumana personaBenef = crearPersona("entidad@email.com", contactoBenef, "Entidad", "Solidaria");
        when(personaRepo.buscarPorRolId(beneficiario.getId())).thenReturn(Optional.of(personaBenef));

        when(donacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        EntregaRealizadaEvent entregaEvent = new EntregaRealizadaEvent(
                donacionId, List.of(), "CC-789-DD", LocalDateTime.now());

        useCase.procesar(entregaEvent);

        ArgumentCaptor<NotificacionEntregaExitosaEvent> captor = ArgumentCaptor.forClass(NotificacionEntregaExitosaEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        NotificacionEntregaExitosaEvent notifEvent = captor.getValue();

        // Donante usa EMAIL
        assertFalse(notifEvent.getContactosDonantes().isEmpty());
        assertEquals("EMAIL", notifEvent.getContactosDonantes().get(0).getMedio());
        assertEquals("donante@email.com", notifEvent.getContactosDonantes().get(0).getDestinatario());

        // Entidad usa SMS (teléfono)
        assertFalse(notifEvent.getContactosEntidades().isEmpty());
        assertEquals("SMS", notifEvent.getContactosEntidades().get(0).getMedio());
        assertEquals("1155550000", notifEvent.getContactosEntidades().get(0).getDestinatario());
    }

    // --- Helper ---

    private PersonaHumana crearPersona(String email, Contacto contacto, String nombre, String apellido) {
        Pais pais = new Pais("Argentina", "Argentino");
        Provincia prov = new Provincia("Buenos Aires", pais);
        Coordenada coord = new Coordenada(-34.0, -58.0);
        Direccion dir = new Direccion("Calle Falsa", 123, "CABA", prov, "1000", coord);
        return new PersonaHumana(email, contacto, dir,
                new DocumentoIdentidad(TipoDocumento.DNI, "12345678"),
                nombre, apellido, 30);
    }
}
