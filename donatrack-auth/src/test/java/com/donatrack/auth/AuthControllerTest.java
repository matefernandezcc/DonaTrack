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
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthControllerTest {

  private AuthService authService;
  private AuthController authController;

  @BeforeEach
  void setUp() throws Exception {
    authService = mock(AuthService.class);
    authController = new AuthController(authService);

    Field webUrlField = AuthController.class.getDeclaredField("webUrl");
    webUrlField.setAccessible(true);
    webUrlField.set(authController, "http://localhost:8080");
  }

  private void setMockSsoEnabled(boolean enabled) throws Exception {
    Field mockSsoField = AuthController.class.getDeclaredField("mockSsoEnabled");
    mockSsoField.setAccessible(true);
    mockSsoField.set(authController, enabled);
  }

  @Test
  @DisplayName("En perfil local (mockSsoEnabled=true), obtenerCodigoSocial genera código 200 OK")
  void testObtenerCodigoSocialEnLocal() throws Exception {
    setMockSsoEnabled(true);
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
        authController.obtenerCodigoSocial("google", "test@test.org", "Test User");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("mock-exchange-code-123", response.getBody().get("code"));
  }

  @Test
  @DisplayName(
      "En perfil prod (mockSsoEnabled=false), obtenerCodigoSocial es bloqueado con 403 Forbidden")
  void testObtenerCodigoSocialEnProdBloqueado() throws Exception {
    setMockSsoEnabled(false);

    ResponseEntity<Map<String, String>> response =
        authController.obtenerCodigoSocial("google", "admin@donatrack.org", "Admin Attack");

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().containsKey("error"));
    verifyNoInteractions(authService);
  }

  @Test
  @DisplayName(
      "En perfil local (mockSsoEnabled=true), mockOAuth2Redirect redirige a callback con código")
  void testMockOAuth2RedirectEnLocal() throws Exception {
    setMockSsoEnabled(true);
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

    authController.mockOAuth2Redirect("google", response);

    verify(response).sendRedirect("http://localhost:8080/auth/callback?code=code-xyz");
  }

  @Test
  @DisplayName(
      "En perfil prod (mockSsoEnabled=false), mockOAuth2Redirect redirige a error de SSO deshabilitado")
  void testMockOAuth2RedirectEnProdDeshabilitado() throws Exception {
    setMockSsoEnabled(false);
    HttpServletResponse response = mock(HttpServletResponse.class);

    authController.mockOAuth2Redirect("google", response);

    verify(response).sendRedirect("http://localhost:8080/login?error=sso_disabled");
    verifyNoInteractions(authService);
  }
}
