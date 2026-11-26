package com.donatrack.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.donatrack.web.gateway.DonacionesGateway;
import com.donatrack.web.gateway.IncentivosGateway;
import com.donatrack.web.gateway.LogisticaGateway;
import com.donatrack.web.service.SolicitudOngService;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
    classes = DonatrackWebApplication.class,
    properties = {
      "donatrack.jwt-secret=SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!"
    })
@AutoConfigureMockMvc
@DisplayName("WebSecurityCsrfTest — Protección CSRF activa en donatrack-web")
class WebSecurityCsrfTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DonacionesGateway donacionesGateway;

  @MockitoBean private IncentivosGateway incentivosGateway;

  @MockitoBean private LogisticaGateway logisticaGateway;

  @MockitoBean private SolicitudOngService solicitudOngService;

  private Cookie obtenerCsrfCookie() throws Exception {
    MvcResult loginResult = mockMvc.perform(get("/login")).andExpect(status().isOk()).andReturn();
    Cookie xsrfCookie = loginResult.getResponse().getCookie("XSRF-TOKEN");
    assertNotNull(xsrfCookie, "GET /login debe emitir cookie XSRF-TOKEN");
    assertFalse(xsrfCookie.getValue().isBlank(), "La cookie XSRF-TOKEN no debe estar vacía");
    return xsrfCookie;
  }

  @Test
  @DisplayName("POST /api/public/chatbot sin token CSRF debe retornar 403 Forbidden")
  void testChatbotSinCsrfRetorna403() throws Exception {
    mockMvc
        .perform(
            post("/api/public/chatbot")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mensaje\":\"hola\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST /api/public/chatbot con token CSRF en cabecera X-XSRF-TOKEN supera CSRF")
  void testChatbotConCsrfNoRetorna403() throws Exception {
    Cookie xsrfCookie = obtenerCsrfCookie();

    MvcResult res =
        mockMvc
            .perform(
                post("/api/public/chatbot")
                    .cookie(xsrfCookie)
                    .header("X-XSRF-TOKEN", xsrfCookie.getValue())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"mensaje\":\"hola\"}"))
            .andReturn();
    assertNotEquals(403, res.getResponse().getStatus());
  }

  @Test
  @DisplayName("POST /api/public/solicitudes-ong sin token CSRF debe retornar 403 Forbidden")
  void testSolicitudesOngSinCsrfRetorna403() throws Exception {
    mockMvc
        .perform(
            post("/api/public/solicitudes-ong")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Comedor Norte\",\"email\":\"norte@ejemplo.org\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST /api/public/solicitudes-ong con token CSRF retorna 200 OK")
  void testSolicitudesOngConCsrfRetorna200() throws Exception {
    Cookie xsrfCookie = obtenerCsrfCookie();
    SolicitudOngService.SolicitudOng solicitud =
        new SolicitudOngService.SolicitudOng(
            "SOL-1",
            "Comedor Norte",
            "30-71112222-9",
            "Av. Norte 100",
            "norte@ejemplo.org",
            "+54 11 4455-6677",
            "Laura Pérez",
            "Comedor",
            "Alimentos",
            "PENDIENTE",
            "2026-11-12 12:00");
    when(solicitudOngService.registrarSolicitud(anyMap())).thenReturn(solicitud);

    mockMvc
        .perform(
            post("/api/public/solicitudes-ong")
                .cookie(xsrfCookie)
                .header("X-XSRF-TOKEN", xsrfCookie.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Comedor Norte\",\"email\":\"norte@ejemplo.org\"}"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("POST /app/donante/donaciones sin CSRF retorna 403 y con CSRF redirige (3xx)")
  void testDonantePostRequiereCsrf() throws Exception {
    UUID personaId = UUID.randomUUID();
    Jwt jwt =
        Jwt.withTokenValue("mock-jwt-csrf")
            .header("alg", "none")
            .subject(UUID.randomUUID().toString())
            .claim("username", "cdonante")
            .claim("personaId", personaId.toString())
            .claim("roles", List.of("DONANTE"))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
    JwtAuthenticationToken donanteAuth =
        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DONANTE")));

    MockHttpSession session = new MockHttpSession();
    session.setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        new SecurityContextImpl(donanteAuth));

    when(donacionesGateway.crearRecepcion(any())).thenReturn(Map.of("id", "REC-123"));

    mockMvc
        .perform(
            post("/app/donante/donaciones")
                .session(session)
                .param("descripcion", "Alimentos")
                .param("cantidad", "5")
                .param("unidadMedicion", "KILOGRAMOS")
                .param("nombreSubcategoria", "No Perecederos"))
        .andExpect(status().isForbidden());

    Cookie xsrfCookie = obtenerCsrfCookie();

    mockMvc
        .perform(
            post("/app/donante/donaciones")
                .session(session)
                .cookie(xsrfCookie)
                .param("_csrf", xsrfCookie.getValue())
                .param("descripcion", "Alimentos")
                .param("cantidad", "5")
                .param("unidadMedicion", "KILOGRAMOS")
                .param("nombreSubcategoria", "No Perecederos"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName(
      "GET /api/notificaciones/mis-alertas sin autenticación pasa filtro de seguridad y retorna 200 OK con lista vacía")
  void testNotificacionesSinAuthRetorna200Vacio() throws Exception {
    mockMvc
        .perform(get("/api/notificaciones/mis-alertas"))
        .andExpect(status().isOk())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath(
                "$", org.hamcrest.Matchers.hasSize(0)));
  }

  @Test
  @DisplayName(
      "GET en ruta protegida /app/admin/dashboard sin autenticación es bloqueada con 403 Forbidden")
  void testRutaAppSinAutenticacionRetorna403() throws Exception {
    mockMvc.perform(get("/app/admin/dashboard")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName(
      "GET en ruta no declarada /api/ruta-inexistente-no-declarada sin autenticación es bloqueada con 403 por política Default Deny")
  void testRutaNoDeclaradaSinAutenticacionRetorna403PorDefaultDeny() throws Exception {
    mockMvc.perform(get("/api/ruta-inexistente-no-declarada")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName(
      "GET /login incluye cabeceras OWASP (CSP, X-Frame-Options, nosniff) y X-Request-Id de trazabilidad")
  void testSecurityAndObservabilityHeadersPresent() throws Exception {
    MvcResult res = mockMvc.perform(get("/login")).andExpect(status().isOk()).andReturn();
    assertNotNull(res.getResponse().getHeader("Content-Security-Policy"));
    assertNotNull(res.getResponse().getHeader("X-Content-Type-Options"));
    assertNotNull(res.getResponse().getHeader("X-Frame-Options"));
    assertNotNull(res.getResponse().getHeader("X-Request-Id"));
  }

  @Test
  @DisplayName(
      "GET /actuator/prometheus es accesible para scraping de Prometheus sin redirección a login")
  void testActuatorPrometheusPermittedForScraper() throws Exception {
    MvcResult res = mockMvc.perform(get("/actuator/prometheus")).andReturn();
    assertNotEquals(401, res.getResponse().getStatus());
    assertNotEquals(403, res.getResponse().getStatus());
  }

  @Test
  @DisplayName(
      "Renderizado real de Thymeleaf sin errores para Home, Novedades, Organizaciones y Paneles")
  void testRealThymeleafTemplatesRenderCleanly() throws Exception {
    when(incentivosGateway.obtenerTop3())
        .thenReturn(
            List.of(
                Map.of("user", "María González", "donations", 12),
                Map.of("user", "Carlos Rodríguez", "donations", 8)));
    when(donacionesGateway.obtenerBeneficiarios()).thenReturn(List.of());
    when(donacionesGateway.obtenerPersonas()).thenReturn(List.of());
    when(donacionesGateway.listarDonaciones(any(), any())).thenReturn(List.of());
    when(logisticaGateway.listarRutas()).thenReturn(List.of());

    mockMvc.perform(get("/")).andExpect(status().isOk());
    mockMvc.perform(get("/novedades")).andExpect(status().isOk());

    Jwt jwtDonante =
        Jwt.withTokenValue("mock-jwt-render")
            .header("alg", "none")
            .subject(UUID.randomUUID().toString())
            .claim("username", "cdonante")
            .claim("roles", List.of("DONANTE"))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
    MockHttpSession sessionDon = new MockHttpSession();
    sessionDon.setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        new SecurityContextImpl(
            new JwtAuthenticationToken(
                jwtDonante, List.of(new SimpleGrantedAuthority("ROLE_DONANTE")))));

    mockMvc
        .perform(get("/app/organizaciones/esperanza").session(sessionDon))
        .andExpect(status().isOk());
    mockMvc.perform(get("/app/donante").session(sessionDon)).andExpect(status().isOk());

    Jwt jwtAdmin =
        Jwt.withTokenValue("mock-jwt-admin-render")
            .header("alg", "none")
            .subject(UUID.randomUUID().toString())
            .claim("username", "admin")
            .claim("roles", List.of("ADMIN"))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
    MockHttpSession sessionAdmin = new MockHttpSession();
    sessionAdmin.setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        new SecurityContextImpl(
            new JwtAuthenticationToken(
                jwtAdmin, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))));

    mockMvc
        .perform(get("/app/admin/donaciones/recepcion").session(sessionAdmin))
        .andExpect(status().isOk());
    mockMvc.perform(get("/app/admin/matchmaking").session(sessionAdmin)).andExpect(status().isOk());
    mockMvc.perform(get("/app/beneficiaria").session(sessionAdmin)).andExpect(status().isOk());
  }
}
