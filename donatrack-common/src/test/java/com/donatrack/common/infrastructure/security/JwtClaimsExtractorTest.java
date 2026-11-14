package com.donatrack.common.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class JwtClaimsExtractorTest {

  private static final String SECRET =
      "SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!";
  private static final String INTERNAL_KEY = "donatrack-test-internal-api-key-32bytes!";

  private String createJwt(UUID userId, UUID personaId, String role, long expSeconds, String secret)
      throws Exception {
    String header =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    StringBuilder payloadBuilder = new StringBuilder("{");
    if (userId != null) {
      payloadBuilder.append("\"sub\":\"").append(userId).append("\",");
    }
    if (personaId != null) {
      payloadBuilder.append("\"personaId\":\"").append(personaId).append("\",");
    }
    if (role != null) {
      payloadBuilder.append("\"roles\":[\"").append(role).append("\"],");
    }
    payloadBuilder.append("\"exp\":").append(expSeconds).append("}");

    String payload =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(payloadBuilder.toString().getBytes(StandardCharsets.UTF_8));
    String data = header + "." + payload;

    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(sig);

    return data + "." + signature;
  }

  @Test
  @DisplayName("Extrae userId y personaId exitosamente de token válido")
  void testExtraerIdsExitoso() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID personaId = UUID.randomUUID();
    long exp = Instant.now().getEpochSecond() + 3600;
    String token = createJwt(userId, personaId, "DONANTE", exp, SECRET);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    Optional<UUID> extractedUser = JwtClaimsExtractor.extraerUserId(request, SECRET);
    Optional<UUID> extractedPersona = JwtClaimsExtractor.extraerPersonaId(request, SECRET);

    assertTrue(extractedUser.isPresent());
    assertEquals(userId, extractedUser.get());

    assertTrue(extractedPersona.isPresent());
    assertEquals(personaId, extractedPersona.get());
  }

  @Test
  @DisplayName("Detecta token expirado y retorna Optional.empty()")
  void testTokenExpirado() throws Exception {
    UUID userId = UUID.randomUUID();
    long exp = Instant.now().getEpochSecond() - 60; // Expirado hace un minuto
    String token = createJwt(userId, null, "DONANTE", exp, SECRET);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    Optional<UUID> extracted = JwtClaimsExtractor.extraerUserId(request, SECRET);
    assertTrue(extracted.isEmpty());
  }

  @Test
  @DisplayName("Detecta firma inválida y retorna Optional.empty()")
  void testFirmaInvalida() throws Exception {
    UUID userId = UUID.randomUUID();
    long exp = Instant.now().getEpochSecond() + 3600;
    String token = createJwt(userId, null, "DONANTE", exp, "clave-incorrecta-diferente-a-la-original-32b");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    Optional<UUID> extracted = JwtClaimsExtractor.extraerUserId(request, SECRET);
    assertTrue(extracted.isEmpty());
  }

  @Test
  @DisplayName("Verifica que X-Service-Key califica como Admin/InternalService")
  void testEsAdminOInternalServiceConServiceKey() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Service-Key", INTERNAL_KEY);

    assertTrue(JwtClaimsExtractor.esAdminOInternalService(request, INTERNAL_KEY, SECRET));
    assertFalse(JwtClaimsExtractor.esAdminOInternalService(request, "clave-invalida", SECRET));
  }

  @Test
  @DisplayName("Verifica que JWT con rol ADMIN y firma válida califica como Admin/InternalService")
  void testEsAdminOInternalServiceConJwtAdmin() throws Exception {
    UUID userId = UUID.randomUUID();
    long exp = Instant.now().getEpochSecond() + 3600;
    String token = createJwt(userId, null, "ADMIN", exp, SECRET);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    assertTrue(JwtClaimsExtractor.esAdminOInternalService(request, INTERNAL_KEY, SECRET));
  }

  @Test
  @DisplayName("JWT con rol ADMIN pero firma inválida es rechazado en esAdminOInternalService")
  void testEsAdminOInternalServiceRechazaFirmaInvalida() throws Exception {
    UUID userId = UUID.randomUUID();
    long exp = Instant.now().getEpochSecond() + 3600;
    String token = createJwt(userId, null, "ADMIN", exp, "clave-falsa-atacante-12345678901234567890");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    assertFalse(JwtClaimsExtractor.esAdminOInternalService(request, INTERNAL_KEY, SECRET));
  }

  @Test
  @DisplayName("Usuario con rol DONANTE no califica como Admin/InternalService")
  void testDonanteNoEsAdmin() throws Exception {
    UUID userId = UUID.randomUUID();
    long exp = Instant.now().getEpochSecond() + 3600;
    String token = createJwt(userId, null, "DONANTE", exp, SECRET);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    assertFalse(JwtClaimsExtractor.esAdminOInternalService(request, INTERNAL_KEY, SECRET));
  }

  @Test
  @DisplayName("Petición sin cabecera Authorization retorna Optional.empty()")
  void testSinCabeceraAuthorizationRetornaEmpty() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    assertTrue(JwtClaimsExtractor.extraerUserId(request, SECRET).isEmpty());
    assertTrue(JwtClaimsExtractor.extraerPersonaId(request, SECRET).isEmpty());
  }

  @Test
  @DisplayName("Cabecera Authorization sin prefijo Bearer retorna Optional.empty()")
  void testCabeceraSinBearerRetornaEmpty() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

    assertTrue(JwtClaimsExtractor.extraerUserId(request, SECRET).isEmpty());
    assertTrue(JwtClaimsExtractor.extraerPersonaId(request, SECRET).isEmpty());
  }

  @Test
  @DisplayName("Token JWT malformado con menos de 3 partes retorna Optional.empty()")
  void testTokenMalformadoRetornaEmpty() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer encabezado.cuerpoSolamente");

    assertTrue(JwtClaimsExtractor.extraerUserId(request, SECRET).isEmpty());
    assertTrue(JwtClaimsExtractor.extraerPersonaId(request, SECRET).isEmpty());
  }

  @Test
  @DisplayName("Token sin claim personaId realiza fallback automático al userId para personaId")
  void testTokenSinPersonaIdFallbackAUserId() throws Exception {
    UUID userId = UUID.randomUUID();
    long exp = Instant.now().getEpochSecond() + 3600;
    String token = createJwt(userId, null, "DONANTE", exp, SECRET);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    assertEquals(Optional.of(userId), JwtClaimsExtractor.extraerUserId(request, SECRET));
    assertEquals(Optional.of(userId), JwtClaimsExtractor.extraerPersonaId(request, SECRET));
  }

  @Test
  @DisplayName("Token sin sub ni personaId retorna Optional.empty() para ambos")
  void testTokenSinSubNiPersonaIdRetornaEmpty() throws Exception {
    long exp = Instant.now().getEpochSecond() + 3600;
    String token = createJwt(null, null, "DONANTE", exp, SECRET);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);

    assertTrue(JwtClaimsExtractor.extraerUserId(request, SECRET).isEmpty());
    assertTrue(JwtClaimsExtractor.extraerPersonaId(request, SECRET).isEmpty());
  }
}
