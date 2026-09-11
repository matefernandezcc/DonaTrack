package com.donatrack.donaciones.application.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProcesarInicioRutaUseCaseTest {

    private DonacionRepository donacionRepo;
    private DonacionOriginalRepository donacionOriginalRepo;
    private PersonaRepository personaRepo;
    private ApplicationEventPublisher eventPublisher;
    private ProcesarInicioRutaUseCase useCase;

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

        useCase = new ProcesarInicioRutaUseCase(donacionRepo, donacionOriginalRepo, personaRepo, eventPublisher);

        Subcategoria sub = new Subcategoria("Alimentos", "Alimentos no perecederos");
        donacion = new Donacion(sub);
        donacionId = donacion.getId();

        beneficiario = new Beneficiario();
        donante = new Donante();
    }

    @Test
    public void donacionesCambianAEnTraslado() {
        when(donacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        useCase.procesar(UUID.randomUUID(), "AA-123-BB", "Carlos", List.of(donacionId));

        assertEquals(EstadoDonacion.EN_TRASLADO, donacion.getEstado());
        verify(donacionRepo).guardar(donacion);
    }

    @Test
    public void sePublicaEventoConContactosDeDonantesYEntidades() {
        // Configurar beneficiario con contacto
        donacion.setEntidadAsignada(beneficiario);

        Contacto contactoBenef = new Contacto("entidad@test.com", "1111", "2222", MedioContacto.CORREO);
        PersonaHumana personaBenef = crearPersona("entidad@test.com", contactoBenef, "Entidad", "Test");
        when(personaRepo.buscarPorRolId(beneficiario.getId())).thenReturn(Optional.of(personaBenef));

        // Configurar donante con contacto
        DonacionOriginal donOriginal = new DonacionOriginal("Desc", donante, "user1");
        when(donacionOriginalRepo.buscarPorIdDonacion(donacionId)).thenReturn(Optional.of(donOriginal));

        Contacto contactoDonante = new Contacto("donante@test.com", "3333", "4444", MedioContacto.CORREO);
        PersonaHumana personaDonante = crearPersona("donante@test.com", contactoDonante, "Donante", "Test");
        when(personaRepo.buscarPorRolId(donante.getId())).thenReturn(Optional.of(personaDonante));

        when(donacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        UUID rutaId = UUID.randomUUID();
        useCase.procesar(rutaId, "AA-123-BB", "Carlos", List.of(donacionId));

        // Verificar que se publicó el evento
        ArgumentCaptor<NotificacionInicioRutaEvent> captor = ArgumentCaptor.forClass(NotificacionInicioRutaEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        NotificacionInicioRutaEvent event = captor.getValue();
        assertEquals(rutaId, event.getRutaId());
        assertEquals("AA-123-BB", event.getPatenteCamion());
        assertEquals("Carlos", event.getNombreChofer());

        // Verificar contactos de donantes
        assertFalse(event.getContactosDonantes().isEmpty());
        assertEquals("donante@test.com", event.getContactosDonantes().get(0).getDestinatario());
        assertEquals("EMAIL", event.getContactosDonantes().get(0).getMedio());
        assertEquals("DONANTE", event.getContactosDonantes().get(0).getRol());

        // Verificar contactos de entidades
        assertFalse(event.getContactosEntidades().isEmpty());
        assertEquals("entidad@test.com", event.getContactosEntidades().get(0).getDestinatario());
        assertEquals("ENTIDAD_BENEFICIARIA", event.getContactosEntidades().get(0).getRol());
    }

    @Test
    public void donacionInexistenteSeSaltea() {
        UUID idInexistente = UUID.randomUUID();
        when(donacionRepo.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> useCase.procesar(UUID.randomUUID(), "AA-123-BB", "Carlos", List.of(idInexistente)));
        verify(donacionRepo, never()).guardar(any());
    }

    @Test
    public void multipleDonacionesSeProcesanCorrectamente() {
        Subcategoria sub2 = new Subcategoria("Ropa", "Ropa usada");
        Donacion donacion2 = new Donacion(sub2);
        UUID donacionId2 = donacion2.getId();

        when(donacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));
        when(donacionRepo.buscarPorId(donacionId2)).thenReturn(Optional.of(donacion2));

        useCase.procesar(UUID.randomUUID(), "AA-123-BB", "Carlos", List.of(donacionId, donacionId2));

        assertEquals(EstadoDonacion.EN_TRASLADO, donacion.getEstado());
        assertEquals(EstadoDonacion.EN_TRASLADO, donacion2.getEstado());
        verify(donacionRepo).guardar(donacion);
        verify(donacionRepo).guardar(donacion2);
    }

    @Test
    public void contactoConMedioTelefonoUsaSMS() {
        donacion.setEntidadAsignada(beneficiario);

        Contacto contactoTel = new Contacto(null, "1155551234", null, MedioContacto.TELEFONO);
        PersonaHumana persona = crearPersona("tel@test.com", contactoTel, "Entidad", "Tel");
        when(personaRepo.buscarPorRolId(beneficiario.getId())).thenReturn(Optional.of(persona));
        when(donacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        useCase.procesar(UUID.randomUUID(), "AA-123-BB", "Carlos", List.of(donacionId));

        ArgumentCaptor<NotificacionInicioRutaEvent> captor = ArgumentCaptor.forClass(NotificacionInicioRutaEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        NotificacionInicioRutaEvent event = captor.getValue();
        assertFalse(event.getContactosEntidades().isEmpty());
        assertEquals("SMS", event.getContactosEntidades().get(0).getMedio());
        assertEquals("1155551234", event.getContactosEntidades().get(0).getDestinatario());
    }

    @Test
    public void contactoConMedioWhatsappUsaWhatsapp() {
        donacion.setEntidadAsignada(beneficiario);

        Contacto contactoWa = new Contacto(null, null, "+5491155551234", MedioContacto.WHATSAPP);
        PersonaHumana persona = crearPersona("wa@test.com", contactoWa, "Entidad", "WA");
        when(personaRepo.buscarPorRolId(beneficiario.getId())).thenReturn(Optional.of(persona));
        when(donacionRepo.buscarPorId(donacionId)).thenReturn(Optional.of(donacion));

        useCase.procesar(UUID.randomUUID(), "AA-123-BB", "Carlos", List.of(donacionId));

        ArgumentCaptor<NotificacionInicioRutaEvent> captor = ArgumentCaptor.forClass(NotificacionInicioRutaEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        NotificacionInicioRutaEvent event = captor.getValue();
        assertFalse(event.getContactosEntidades().isEmpty());
        assertEquals("WHATSAPP", event.getContactosEntidades().get(0).getMedio());
        assertEquals("+5491155551234", event.getContactosEntidades().get(0).getDestinatario());
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
