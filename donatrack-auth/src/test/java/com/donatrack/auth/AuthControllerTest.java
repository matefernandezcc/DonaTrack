package com.donatrack.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.donatrack.auth.application.service.AuthService;
import com.donatrack.auth.domain.entities.Rol;
import com.donatrack.auth.domain.entities.Usuario;
import com.donatrack.auth.infrastructure.adapters.in.api.AuthController;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthControllerTest {

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = mock(AuthService.class);
  }

  private AuthController createController(boolean mockSsoEnabled) {
    return new AuthController(authService, "http://localhost:8080", mockSsoEnabled);
  }

  @Test
  @DisplayName("En perfil local (mockSsoEnabled=true), obtenerCodigoSocial genera código 200 OK")
  void testObtenerCodigoSocialEnLocal() {
    AuthController controller = createController(true);
    Usuario mockUsuario =
        Usuario.builder()
            .id(UUID.randomUUID())
            .username("donante.google@gmail.com")
            .email("donante.google@gmail.com")
            .rol(Rol.DONANTE)
            .build();

    when(authService.obtenerOCrearUsuarioSocial(anyString(), anyString(), anyString()))
        .thenReturn(mockUsuario);
    when(authService.generarCodigoIntercambio(any())).thenReturn("mock-exchange-code-123");

    ResponseEntity<Map<String, String>> response =
        controller.obtenerCodigoSocial("google", "test@test.org", "Test User");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("mock-exchange-code-123", response.getBody().get("code"));
  }

  @Test
  @DisplayName(
      "En perfil local con provider github y sin email/nombre usa valores predeterminados de prueba")
  void testObtenerCodigoSocialGithubValoresDefault() {
    AuthController controller = createController(true);
    Usuario mockUsuario =
        Usuario.builder()
            .id(UUID.randomUUID())
            .username("donante.github@gmail.com")
            .email("donante.github@gmail.com")
            .rol(Rol.DONANTE)
            .build();

    when(authService.obtenerOCrearUsuarioSocial(
            eq("donante.github@gmail.com"), eq("Donante GitHub (SSO)"), eq("GITHUB")))
        .thenReturn(mockUsuario);
    when(authService.generarCodigoIntercambio(any())).thenReturn("github-code-456");

    ResponseEntity<Map<String, String>> response =
        controller.obtenerCodigoSocial("github", null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("github-code-456", response.getBody().get("code"));
  }

  @Test
  @DisplayName(
      "En perfil prod (mockSsoEnabled=false), obtenerCodigoSocial es bloqueado con 403 Forbidden")
  void testObtenerCodigoSocialEnProdBloqueado() {
    AuthController controller = createController(false);

    ResponseEntity<Map<String, String>> response =
        controller.obtenerCodigoSocial("google", "admin@donatrack.org", "Admin Attack");

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().containsKey("error"));
    verifyNoInteractions(authService);
  }

  @Test
  @DisplayName(
      "En perfil local (mockSsoEnabled=true), mockOAuth2Redirect redirige a callback con código")
  void testMockOAuth2RedirectEnLocal() throws IOException {
    AuthController controller = createController(true);
    HttpServletResponse response = mock(HttpServletResponse.class);

    Usuario mockUsuario =
        Usuario.builder()
            .id(UUID.randomUUID())
            .username("donante.google@gmail.com")
            .email("donante.google@gmail.com")
            .rol(Rol.DONANTE)
            .build();

    when(authService.obtenerOCrearUsuarioSocial(anyString(), anyString(), anyString()))
        .thenReturn(mockUsuario);
    when(authService.generarCodigoIntercambio(any())).thenReturn("code-xyz");

    controller.mockOAuth2Redirect("google", response);

    verify(response).sendRedirect("http://localhost:8080/auth/callback?code=code-xyz");
  }

  @Test
  @DisplayName(
      "En perfil prod (mockSsoEnabled=false), mockOAuth2Redirect redirige a error de SSO deshabilitado")
  void testMockOAuth2RedirectEnProdDeshabilitado() throws IOException {
    AuthController controller = createController(false);
    HttpServletResponse response = mock(HttpServletResponse.class);

    controller.mockOAuth2Redirect("google", response);

    verify(response).sendRedirect("http://localhost:8080/login?error=sso_disabled");
    verifyNoInteractions(authService);
  }
}
