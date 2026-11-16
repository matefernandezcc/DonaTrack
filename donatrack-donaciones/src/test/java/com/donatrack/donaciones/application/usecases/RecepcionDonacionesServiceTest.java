package com.donatrack.donaciones.application.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.donatrack.donaciones.application.ports.in.BienDTO;
import com.donatrack.donaciones.application.ports.in.CargaBienesRequestDTO;
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
import com.donatrack.donaciones.domain.entities.persona.ubicacion.Coordenada;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.Direccion;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.Pais;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.Provincia;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecepcionDonacionesService — Casos de Uso de Carga y Recepción en Depósito")
class RecepcionDonacionesServiceTest {

  private PersonaRepository personaRepository;
  private ProcesadorCargaInicial procesadorCargaInicial;
  private DonacionRepository donacionRepository;
  private DonacionOriginalRepository recepcionDonacionRepository;
  private RecepcionDonacionesService service;

  @BeforeEach
  void setUp() {
    personaRepository = mock(PersonaRepository.class);
    procesadorCargaInicial = mock(ProcesadorCargaInicial.class);
    donacionRepository = mock(DonacionRepository.class);
    recepcionDonacionRepository = mock(DonacionOriginalRepository.class);

    service =
        new RecepcionDonacionesService(
            personaRepository,
            procesadorCargaInicial,
            donacionRepository,
            recepcionDonacionRepository);
  }

  private PersonaHumana crearPersona(String email) {
    Pais pais = new Pais("Argentina", "Argentino");
    Provincia prov = new Provincia("Buenos Aires", pais);
    Coordenada coord = new Coordenada(-34.6037, -58.3816);
    Direccion dir = new Direccion("Av. Corrientes", 1234, "CABA", prov, "1043", coord);
    Contacto contacto = new Contacto(email, "1122334455", null, MedioContacto.CORREO);
    return new PersonaHumana(
        email,
        contacto,
        dir,
        new DocumentoIdentidad(TipoDocumento.DNI, "30123456"),
        "Juan",
        "Pérez",
        35);
  }

  @Test
  @DisplayName("Si la persona donante no tiene rol Donante, se le asigna automáticamente y se persiste")
  void recibir_personaDonanteSinRolDonante_asignaRolAutomaticamenteYPersiste() {
    // Arrange
    UUID donanteId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();
    PersonaHumana donantePersona = crearPersona("nuevo.donante@test.com");
    PersonaHumana adminPersona = crearPersona("admin@donatrack.org");

    assertTrue(
        donantePersona.getRoles().stream().noneMatch(r -> r instanceof Donante),
        "La persona inicialmente no debe tener rol Donante");

    when(personaRepository.buscarPorId(donanteId)).thenReturn(Optional.of(donantePersona));
    when(personaRepository.buscarPorId(adminId)).thenReturn(Optional.of(adminPersona));

    Donacion donacionSegmentada = new Donacion(new Subcategoria("Alimentos", "No perecederos"));
    when(procesadorCargaInicial.procesar(any())).thenReturn(List.of(donacionSegmentada));

    BienDTO bienDTO =
        new BienDTO("Fideos Secos", 10.0, "PAQUETES", false, null, "Alimentos");
    CargaBienesRequestDTO request =
        new CargaBienesRequestDTO(donanteId, adminId, List.of(bienDTO));

    // Act
    DonacionOriginal resultado = service.recibir(request);

    // Assert
    assertNotNull(resultado);
    assertTrue(
        donantePersona.getRoles().stream().anyMatch(r -> r instanceof Donante),
        "Se debió agregar el rol Donante a la persona");
    verify(personaRepository, times(1)).guardar(donantePersona);
    verify(recepcionDonacionRepository, times(1)).guardar(resultado);
    assertEquals(1, resultado.getDonacionesSegmentadas().size());
    assertEquals(
        EstadoDonacion.EN_DEPOSITO,
        resultado.getDonacionesSegmentadas().get(0).getEstado());
  }

  @Test
  @DisplayName("Si la persona ya tiene rol Donante, se reutiliza sin duplicar ni re-guardar la persona")
  void recibir_personaDonanteYaConRolDonante_reutilizaRolSinDuplicar() {
    // Arrange
    UUID donanteId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();
    PersonaHumana donantePersona = crearPersona("donante.veterano@test.com");
    Donante rolExistente = new Donante();
    donantePersona.agregarRol(rolExistente);

    when(personaRepository.buscarPorId(donanteId)).thenReturn(Optional.of(donantePersona));
    when(personaRepository.buscarPorId(adminId)).thenReturn(Optional.of(crearPersona("admin@donatrack.org")));

    Donacion donacionSegmentada = new Donacion(new Subcategoria("Abrigo", "Frazadas"));
    when(procesadorCargaInicial.procesar(any())).thenReturn(List.of(donacionSegmentada));

    BienDTO bienDTO =
        new BienDTO("Frazadas polares", 5.0, "UNIDADES", false, null, "Abrigo");
    CargaBienesRequestDTO request =
        new CargaBienesRequestDTO(donanteId, adminId, List.of(bienDTO));

    // Act
    DonacionOriginal resultado = service.recibir(request);

    // Assert
    assertNotNull(resultado);
    long countDonante =
        donantePersona.getRoles().stream().filter(r -> r instanceof Donante).count();
    assertEquals(1, countDonante, "No debe duplicarse el rol Donante");
    verify(personaRepository, never()).guardar(donantePersona);
    verify(recepcionDonacionRepository, times(1)).guardar(resultado);
  }

  @Test
  @DisplayName("Bienes no perecederos con fechaVencimiento null se mapean y procesan correctamente")
  void recibir_bienConFechaVencimientoNull_procesaCorrectamente() {
    // Arrange
    UUID donanteId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();
    PersonaHumana donantePersona = crearPersona("donante@test.com");
    donantePersona.agregarRol(new Donante());

    when(personaRepository.buscarPorId(donanteId)).thenReturn(Optional.of(donantePersona));
    when(personaRepository.buscarPorId(adminId)).thenReturn(Optional.of(crearPersona("admin@donatrack.org")));

    Donacion donacionSegmentada = new Donacion(new Subcategoria("Higiene", "Jabón"));
    when(procesadorCargaInicial.procesar(any())).thenReturn(List.of(donacionSegmentada));

    BienDTO bienSinVencimiento =
        new BienDTO("Jabón en pan", 20.0, "UNIDADES", false, null, "Higiene");
    CargaBienesRequestDTO request =
        new CargaBienesRequestDTO(donanteId, adminId, List.of(bienSinVencimiento));

    // Act
    DonacionOriginal resultado = service.recibir(request);

    // Assert
    assertNotNull(resultado);
    verify(procesadorCargaInicial, times(1))
        .procesar(
            argThat(
                bienes ->
                    bienes.size() == 1
                        && bienes.get(0).getFechaVencimiento() == null
                        && bienes.get(0).getDescripcion().equals("Jabón en pan")));
    verify(recepcionDonacionRepository, times(1)).guardar(resultado);
  }

  @Test
  @DisplayName("Bienes perecederos con fechaVencimiento específica conservan la fecha")
  void recibir_bienConFechaVencimiento_conservaFecha() {
    // Arrange
    UUID donanteId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();
    PersonaHumana donantePersona = crearPersona("donante@test.com");
    donantePersona.agregarRol(new Donante());

    LocalDate fechaVenc = LocalDate.now().plusMonths(3);

    when(personaRepository.buscarPorId(donanteId)).thenReturn(Optional.of(donantePersona));
    when(personaRepository.buscarPorId(adminId)).thenReturn(Optional.of(crearPersona("admin@donatrack.org")));

    Donacion donacionSegmentada = new Donacion(new Subcategoria("Lácteos", "Leche"));
    when(procesadorCargaInicial.procesar(any())).thenReturn(List.of(donacionSegmentada));

    BienDTO bienPerecedero =
        new BienDTO("Leche UAT", 30.0, "LITROS", false, fechaVenc, "Lácteos");
    CargaBienesRequestDTO request =
        new CargaBienesRequestDTO(donanteId, adminId, List.of(bienPerecedero));

    // Act
    DonacionOriginal resultado = service.recibir(request);

    // Assert
    assertNotNull(resultado);
    verify(procesadorCargaInicial, times(1))
        .procesar(
            argThat(
                bienes ->
                    bienes.size() == 1
                        && fechaVenc.equals(bienes.get(0).getFechaVencimiento())
                        && bienes.get(0).getDescripcion().equals("Leche UAT")));
  }

  @Test
  @DisplayName("Donante inexistente ni en ID ni en default arroja IllegalArgumentException")
  void recibir_donanteInexistente_lanzaIllegalArgumentException() {
    // Arrange
    UUID idInexistente = UUID.randomUUID();
    when(personaRepository.buscarPorId(any())).thenReturn(Optional.empty());

    CargaBienesRequestDTO request =
        new CargaBienesRequestDTO(idInexistente, UUID.randomUUID(), List.of());

    // Act & Assert
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> service.recibir(request));
    assertEquals("Donante no encontrado", ex.getMessage());
    verify(recepcionDonacionRepository, never()).guardar(any());
  }
}
