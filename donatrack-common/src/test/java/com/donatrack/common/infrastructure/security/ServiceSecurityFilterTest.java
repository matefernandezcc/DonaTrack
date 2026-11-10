package com.donatrack.common.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ServiceSecurityFilterTest {

  private static final String TEST_API_KEY = "test-internal-key-12345";
  private static final String TEST_JWT_SECRET = "super-secret-key-that-is-at-least-32-bytes-long!";

  private ServiceSecurityFilter filterProd;
  private ServiceSecurityFilter filterLocal;

  @BeforeEach
  void setUp() {
    filterProd = new ServiceSecurityFilter(true, TEST_API_KEY, TEST_JWT_SECRET);
    filterLocal = new ServiceSecurityFilter(false, TEST_API_KEY, TEST_JWT_SECRET);
  }

  private String generateJwt(long expEpochSeconds, String secret) throws Exception {
    String header =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    String payload =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                ("{\"sub\":\"test-user\",\"exp\":" + expEpochSeconds + "}")
                    .getBytes(StandardCharsets.UTF_8));
    String data = header + "." + payload;

    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);

    return data + "." + signature;
  }

  @Test
  @DisplayName(
      "En perfil local (securityEnabled=false), solicitudes pasan libremente sin autenticación")
  void testSecurityDisabledAllowsAll() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/donaciones/123");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filterLocal.doFilter(request, response, filterChain);

    assertEquals(200, response.getStatus());
    assertNotNull(filterChain.getRequest());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/actuator/health",
        "/v3/api-docs",
        "/swagger-ui/index.html",
        "/auth/login",
        "/auth/register",
        "/oauth2/authorization/google",
        "/healthcheck",
        "/api/auth/sso/codigo"
      })
  @DisplayName("En perfil prod, rutas públicas pasan sin credenciales")
  void testPublicPathsPassInProd(String path) throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filterProd.doFilter(request, response, filterChain);

    assertEquals(200, response.getStatus(), "Path " + path + " debería ser público");
    assertNotNull(filterChain.getRequest());
  }

  @Test
  @DisplayName(
      "En perfil prod, endpoint sensible /api/auth/perfil/{id} queda protegido contra acceso anónimo")
  void testPerfilEndpointIsProtectedInProd() throws ServletException, IOException {
    MockHttpServletRequest request =
        new MockHttpServletRequest("GET", "/api/auth/perfil/a1111111-1111-4111-8111-111111111111");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filterProd.doFilter(request, response, filterChain);

    assertEquals(401, response.getStatus(), "/api/auth/perfil no debe ser público");
    assertNull(filterChain.getRequest());
  }

  @Test
  @DisplayName("En perfil prod, ruta protegida sin credenciales retorna 401 Unauthorized")
  void testProtectedPathWithoutCredentialsReturns401() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/donaciones");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filterProd.doFilter(request, response, filterChain);

    assertEquals(401, response.getStatus());
    assertTrue(response.getContentType().contains("application/json"));
    assertEquals("UTF-8", response.getCharacterEncoding());
    assertTrue(response.getContentAsString().contains("Unauthorized"));
    assertNull(filterChain.getRequest(), "La cadena no debió avanzar");
  }

  @Test
  @DisplayName("En perfil prod, cabecera X-Service-Key válida permite acceso")
  void testValidServiceKeyAllowsAccess() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/donaciones");
    request.addHeader("X-Service-Key", TEST_API_KEY);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filterProd.doFilter(request, response, filterChain);

    assertEquals(200, response.getStatus());
    assertNotNull(filterChain.getRequest());
  }

  @Test
  @DisplayName("En perfil prod, cabecera X-Service-Key inválida es bloqueada con 401")
  void testInvalidServiceKeyReturns401() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/donaciones");
    request.addHeader("X-Service-Key", "wrong-key");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filterProd.doFilter(request, response, filterChain);

    assertEquals(401, response.getStatus());
    assertNull(filterChain.getRequest());
  }

  @Test
  @DisplayName("En perfil prod, token JWT Bearer válido permite acceso")
  void testValidBearerJwtAllowsAccess() throws Exception {
    String validToken = generateJwt(Instant.now().getEpochSecond() + 3600, TEST_JWT_SECRET);

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/donaciones");
    request.addHeader("Authorization", "Bearer " + validToken);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filterProd.doFilter(request, response, filterChain);

    assertEquals(200, response.getStatus());
    assertNotNull(filterChain.getRequest());
  }

  @Test
  @DisplayName("En perfil prod, token JWT Bearer expirado es rechazado con 401")
  void testExpiredBearerJwtReturns401() throws Exception {
    String expiredToken = generateJwt(Instant.now().getEpochSecond() - 3600, TEST_JWT_SECRET);

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/donaciones");
    request.addHeader("Authorization", "Bearer " + expiredToken);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filterProd.doFilter(request, response, filterChain);

    assertEquals(401, response.getStatus());
    assertNull(filterChain.getRequest());
  }
}
