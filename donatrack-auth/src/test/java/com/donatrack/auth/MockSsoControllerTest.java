package com.donatrack.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.donatrack.auth.application.service.AuthService;
import com.donatrack.auth.domain.entities.Rol;
import com.donatrack.auth.domain.entities.Usuario;
import com.donatrack.auth.infrastructure.adapters.in.api.MockSsoController;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class MockSsoControllerTest {

  private AuthService authService;
  private MockSsoController controller;

  @BeforeEach
  void setUp() {
    authService = mock(AuthService.class);
    controller = new MockSsoController(authService, "http://localhost:8080");
  }

  @Test
  @DisplayName("MockSsoController: obtenerCodigoSocial genera código 200 OK")
  void testObtenerCodigoSocialEnLocal() {
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
  @DisplayName("MockSsoController: provider github sin email usa valores predeterminados")
  void testObtenerCodigoSocialGithubValoresDefault() {
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
  @DisplayName("MockSsoController: mockOAuth2Redirect redirige a callback con código")
  void testMockOAuth2RedirectEnLocal() throws IOException {
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
}
