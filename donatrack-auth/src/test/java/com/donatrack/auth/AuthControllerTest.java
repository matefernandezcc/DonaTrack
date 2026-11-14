package com.donatrack.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.donatrack.auth.application.service.AuthService;
import com.donatrack.auth.infrastructure.adapters.in.api.AuthController;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.LoginRequest;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.TokenResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class AuthControllerTest {

  private static final String SECRET =
      "SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!";
  private static final String SERVICE_KEY = "donatrack-internal-key-change-in-prod";

  private AuthService authService;
  private AuthController controller;

  @BeforeEach
  void setUp() {
    authService = mock(AuthService.class);
    controller = new AuthController(authService, "http://localhost:8080", SECRET, SERVICE_KEY);
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
  @DisplayName("Login con credenciales válidas retorna 200 OK y token")
  void testLoginExitoso() {
    LoginRequest request = new LoginRequest("donante", "donante");
    com.donatrack.auth.infrastructure.adapters.in.api.dto.UserInfoDto userInfo =
        new com.donatrack.auth.infrastructure.adapters.in.api.dto.UserInfoDto(
            UUID.randomUUID(), "donante", "Donante", "donante@donatrack.org", "DONANTE", UUID.randomUUID(), null, null);
    TokenResponse tokenResponse =
        new TokenResponse("mock-jwt-token", "Bearer", 7200L, userInfo);
    when(authService.login(request)).thenReturn(tokenResponse);

    ResponseEntity<TokenResponse> response = controller.login(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("mock-jwt-token", response.getBody().accessToken());
  }

  @Test
  @DisplayName("Anti-IDOR: Usuario donante puede consultar su propio perfil (200 OK)")
  void testObtenerPerfilPropioPermitido() throws Exception {
    UUID myUserId = UUID.randomUUID();
    String token = createJwt(myUserId, myUserId, "DONANTE");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    when(authService.obtenerPerfil(myUserId)).thenReturn(Map.of("id", myUserId, "nombre", "Mi Perfil"));

    ResponseEntity<Map<String, Object>> response = controller.obtenerPerfil(myUserId, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Mi Perfil", response.getBody().get("nombre"));
  }

  @Test
  @DisplayName("Anti-IDOR: Usuario donante recibe 403 Forbidden al intentar ver perfil ajeno")
  void testObtenerPerfilAjenoBloqueado() throws Exception {
    UUID myUserId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    String token = createJwt(myUserId, myUserId, "DONANTE");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    ResponseEntity<Map<String, Object>> response = controller.obtenerPerfil(otherUserId, request);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(authService, never()).obtenerPerfil(any());
  }

  @Test
  @DisplayName("Anti-IDOR: Administrador puede consultar perfiles de otros usuarios (200 OK)")
  void testAdminPuedeConsultarPerfilAjeno() throws Exception {
    UUID adminUserId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    String token = createJwt(adminUserId, null, "ADMIN");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    when(authService.obtenerPerfil(targetUserId))
        .thenReturn(Map.of("id", targetUserId, "nombre", "Usuario Objetivo"));

    ResponseEntity<Map<String, Object>> response = controller.obtenerPerfil(targetUserId, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Usuario Objetivo", response.getBody().get("nombre"));
  }
}
