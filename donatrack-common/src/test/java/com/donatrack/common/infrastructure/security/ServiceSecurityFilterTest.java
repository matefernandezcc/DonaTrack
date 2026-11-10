package com.donatrack.common.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ServiceSecurityFilterTest {

  private ServiceSecurityFilter filter;
  private static final String TEST_API_KEY = "test-internal-key-12345";
  private static final String TEST_JWT_SECRET = "super-secret-key-that-is-at-least-32-bytes-long!";

  @BeforeEach
  void setUp() throws Exception {
    filter = new ServiceSecurityFilter();

    setField(filter, "internalApiKey", TEST_API_KEY);
    setField(filter, "jwtSecret", TEST_JWT_SECRET);
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = ServiceSecurityFilter.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  @DisplayName(
      "En perfil local (securityEnabled=false), solicitudes pasan libremente sin autenticación")
  void testSecurityDisabledAllowsAll() throws ServletException, IOException, Exception {
    setField(filter, "securityEnabled", false);

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/donaciones/123");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filter.doFilter(request, response, filterChain);

    assertEquals(200, response.getStatus());
    assertNotNull(filterChain.getRequest());
  }

  @Test
  @DisplayName("En perfil prod (securityEnabled=true), rutas públicas pasan sin credenciales")
  void testPublicPathsPassInProd() throws ServletException, IOException, Exception {
    setField(filter, "securityEnabled", true);

    String[] publicPaths = {
      "/actuator/health",
      "/v3/api-docs",
      "/swagger-ui/index.html",
      "/auth/login",
      "/oauth2/authorization/google",
      "/healthcheck",
      "/api/auth/sso/codigo"
    };

    for (String path : publicPaths) {
      MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
      MockHttpServletResponse response = new MockHttpServletResponse();
      MockFilterChain filterChain = new MockFilterChain();

      filter.doFilter(request, response, filterChain);

      assertEquals(200, response.getStatus(), "Path " + path + " debería ser público");
      assertNotNull(filterChain.getRequest());
    }
  }

  @Test
  @DisplayName(
      "En perfil prod (securityEnabled=true), ruta protegida sin credenciales retorna 401 Unauthorized")
  void testProtectedPathWithoutCredentialsReturns401()
      throws ServletException, IOException, Exception {
    setField(filter, "securityEnabled", true);

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/donaciones");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filter.doFilter(request, response, filterChain);

    assertEquals(401, response.getStatus());
    assertTrue(response.getContentAsString().contains("Unauthorized"));
    assertNull(filterChain.getRequest(), "La cadena no debió avanzar");
  }

  @Test
  @DisplayName(
      "En perfil prod (securityEnabled=true), cabecera X-Service-Key válida permite acceso")
  void testValidServiceKeyAllowsAccess() throws ServletException, IOException, Exception {
    setField(filter, "securityEnabled", true);

    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/donaciones");
    request.addHeader("X-Service-Key", TEST_API_KEY);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filter.doFilter(request, response, filterChain);

    assertEquals(200, response.getStatus());
    assertNotNull(filterChain.getRequest());
  }

  @Test
  @DisplayName(
      "En perfil prod (securityEnabled=true), cabecera X-Service-Key inválida es bloqueada con 401")
  void testInvalidServiceKeyReturns401() throws ServletException, IOException, Exception {
    setField(filter, "securityEnabled", true);

    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/donaciones");
    request.addHeader("X-Service-Key", "wrong-key");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filter.doFilter(request, response, filterChain);

    assertEquals(401, response.getStatus());
    assertNull(filterChain.getRequest());
  }
}
