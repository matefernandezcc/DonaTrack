package com.donatrack.donaciones.infrastructure.adapters.in.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.donatrack.donaciones.application.ports.in.DonacionResponseDTO;
import com.donatrack.donaciones.application.ports.out.BeneficiarioRepository;
import com.donatrack.donaciones.application.ports.out.DonacionOriginalRepository;
import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.application.ports.out.ServicioNotificaciones;
import com.donatrack.donaciones.application.usecases.AsignacionBatchJob;
import com.donatrack.donaciones.application.usecases.AuditoriaDepositoJob;
import com.donatrack.donaciones.application.ports.in.RecepcionDonacionesUseCase;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.services.MatchmakerService;
import com.donatrack.donaciones.infrastructure.adapters.out.client.IncentivoClient;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class DonacionControllerSecurityTest {

  private static final String SECRET =
      "SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!";
  private static final String SERVICE_KEY = "donatrack-internal-key-change-in-prod";
  // The fallback donor ID used when no repository matches
  private static final UUID DEFAULT_DONOR_PERSONA_ID =
      UUID.fromString("a1111111-1111-4111-8111-111111111111");

  private DonacionRepository donacionRepository;
  private DonacionController controller;

  @BeforeEach
  void setUp() {
    donacionRepository = mock(DonacionRepository.class);
    MatchmakerService matchmakerService = mock(MatchmakerService.class);
    ServicioNotificaciones servicioNotificaciones = mock(ServicioNotificaciones.class);
    IncentivoClient incentivoClient = mock(IncentivoClient.class);
    BeneficiarioRepository beneficiarioRepository = mock(BeneficiarioRepository.class);
    AsignacionBatchJob asignacionBatchJob = mock(AsignacionBatchJob.class);
    RecepcionDonacionesUseCase recepcionDonacionesUseCase = mock(RecepcionDonacionesUseCase.class);
    AuditoriaDepositoJob auditoriaDepositoJob = mock(AuditoriaDepositoJob.class);
    DonacionOriginalRepository donacionOriginalRepository = mock(DonacionOriginalRepository.class);
    PersonaRepository personaRepository = mock(PersonaRepository.class);

    controller =
        new DonacionController(
            matchmakerService,
            servicioNotificaciones,
            incentivoClient,
            donacionRepository,
            beneficiarioRepository,
            asignacionBatchJob,
            recepcionDonacionesUseCase,
            auditoriaDepositoJob,
            donacionOriginalRepository,
            personaRepository,
            null);
  }

  private String createJwt(UUID userId, UUID personaId, String role) throws Exception {
    String header =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    long exp = Instant.now().getEpochSecond() + 3600;
    String payload =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                ("{\"sub\":\""
                        + userId
                        + "\",\"personaId\":\""
                        + (personaId != null ? personaId : userId)
                        + "\",\"roles\":[\""
                        + role
                        + "\"],\"exp\":"
                        + exp
                        + "}")
                    .getBytes(StandardCharsets.UTF_8));
    String data = header + "." + payload;
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    String sig =
        Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    return data + "." + sig;
  }

  @Test
  @DisplayName("Anti-IDOR: Donante dueño puede consultar su propia donación (200 OK)")
  void testDuenoPuedeConsultarDonacion() throws Exception {
    UUID donacionId = UUID.randomUUID();
    String token = createJwt(UUID.randomUUID(), DEFAULT_DONOR_PERSONA_ID, "DONANTE");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    Donacion mockDonacion = mock(Donacion.class);
    when(mockDonacion.getId()).thenReturn(donacionId);
    when(mockDonacion.getEstado()).thenReturn(EstadoDonacion.EN_DEPOSITO);
    when(donacionRepository.buscarPorId(donacionId)).thenReturn(Optional.of(mockDonacion));

    ResponseEntity<DonacionResponseDTO> response = controller.obtenerDonacion(donacionId, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("Anti-IDOR: Donante ajeno recibe 403 Forbidden")
  void testDonanteAjenoBloqueado() throws Exception {
    UUID donacionId = UUID.randomUUID();
    UUID alienDonorPersonaId = UUID.randomUUID();
    String token = createJwt(UUID.randomUUID(), alienDonorPersonaId, "DONANTE");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    ResponseEntity<DonacionResponseDTO> response = controller.obtenerDonacion(donacionId, request);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(donacionRepository, never()).buscarPorId(any());
  }

  @Test
  @DisplayName("Anti-IDOR: Administrador puede consultar cualquier donación (200 OK)")
  void testAdminPuedeConsultarCualquierDonacion() throws Exception {
    UUID donacionId = UUID.randomUUID();
    String token = createJwt(UUID.randomUUID(), null, "ADMIN");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    Donacion mockDonacion = mock(Donacion.class);
    when(mockDonacion.getId()).thenReturn(donacionId);
    when(mockDonacion.getEstado()).thenReturn(EstadoDonacion.EN_DEPOSITO);
    when(donacionRepository.buscarPorId(donacionId)).thenReturn(Optional.of(mockDonacion));

    ResponseEntity<DonacionResponseDTO> response = controller.obtenerDonacion(donacionId, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("Anti-IDOR: Servicio interno con X-Service-Key puede consultar cualquier donación (200 OK)")
  void testServicioInternoConServiceKeyPuedeConsultar() {
    UUID donacionId = UUID.randomUUID();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Service-Key", SERVICE_KEY);

    Donacion mockDonacion = mock(Donacion.class);
    when(mockDonacion.getId()).thenReturn(donacionId);
    when(mockDonacion.getEstado()).thenReturn(EstadoDonacion.EN_DEPOSITO);
    when(donacionRepository.buscarPorId(donacionId)).thenReturn(Optional.of(mockDonacion));

    ResponseEntity<DonacionResponseDTO> response = controller.obtenerDonacion(donacionId, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
