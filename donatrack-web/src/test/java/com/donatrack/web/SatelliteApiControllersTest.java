package com.donatrack.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.donatrack.web.controller.api.DonanteApiController;
import com.donatrack.web.controller.api.NotificacionesApiController;
import com.donatrack.web.controller.api.PublicApiController;
import com.donatrack.web.gateway.DonacionesGateway;
import com.donatrack.web.gateway.LogisticaGateway;
import com.donatrack.web.gateway.NotificacionesGateway;
import com.donatrack.web.service.CacheResilienteService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@DisplayName("SatelliteApiControllers & CacheResilienteService — Tests Unitarios")
class SatelliteApiControllersTest {

  private NotificacionesGateway notificacionesGateway;
  private DonacionesGateway donacionesGateway;
  private LogisticaGateway logisticaGateway;
  private CacheResilienteService cacheResilienteService;

  private MockMvc notificacionesMvc;
  private MockMvc donanteMvc;
  private MockMvc publicMvc;

  @BeforeEach
  void setUp() {
    notificacionesGateway = mock(NotificacionesGateway.class);
    donacionesGateway = mock(DonacionesGateway.class);
    logisticaGateway = mock(LogisticaGateway.class);

    cacheResilienteService = new CacheResilienteService(logisticaGateway);

    NotificacionesApiController notificacionesController =
        new NotificacionesApiController(notificacionesGateway);
    DonanteApiController donanteController =
        new DonanteApiController(cacheResilienteService, donacionesGateway);
    PublicApiController publicController =
        new PublicApiController(cacheResilienteService, donacionesGateway);

    notificacionesMvc = MockMvcBuilders.standaloneSetup(notificacionesController).build();
    donanteMvc = MockMvcBuilders.standaloneSetup(donanteController).build();
    publicMvc = MockMvcBuilders.standaloneSetup(publicController).build();
  }

  @Test
  @DisplayName("GET /api/notificaciones/mis-alertas — Retorna alertas del usuario autenticado")
  void testMisAlertas() throws Exception {
    when(notificacionesGateway.obtenerNotificacionesUsuario("donante@test.com"))
        .thenReturn(
            List.of(
                Map.of(
                    "mensaje", "Tu donación ha sido asignada al Comedor El Faro",
                    "medio", "EMAIL",
                    "completada", true)));

    Jwt jwt =
        Jwt.withTokenValue("mock-token")
            .header("alg", "none")
            .claim("sub", UUID.randomUUID().toString())
            .claim("email", "donante@test.com")
            .build();
    JwtAuthenticationToken auth =
        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DONANTE")));

    notificacionesMvc
        .perform(get("/api/notificaciones/mis-alertas").principal(auth))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$[0].mensaje").value("Tu donación ha sido asignada al Comedor El Faro"))
        .andExpect(jsonPath("$[0].medio").value("EMAIL"));
  }

  @Test
  @DisplayName(
      "GET /api/notificaciones/mis-alertas — Retorna lista vacía si auth es nulo o sin email")
  void testMisAlertasSinAuth() throws Exception {
    notificacionesMvc
        .perform(get("/api/notificaciones/mis-alertas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
  }

  @Test
  @DisplayName(
      "GET /api/notificaciones/mis-alertas — Consulta email real en DonacionesGateway y técnico sin duplicar")
  void testMisAlertasConDonacionesGatewayMultiDestinatario() throws Exception {
    UUID personaId = UUID.randomUUID();
    NotificacionesApiController controllerConDonaciones =
        new NotificacionesApiController(notificacionesGateway, donacionesGateway);
    MockMvc mvcConDonaciones = MockMvcBuilders.standaloneSetup(controllerConDonaciones).build();

    when(donacionesGateway.obtenerPersona(personaId))
        .thenReturn(Map.of("email", "persona.real@ejemplo.org"));
    when(notificacionesGateway.obtenerNotificacionesUsuario(
            List.of("donante" + personaId + "@test.com", "persona.real@ejemplo.org")))
        .thenReturn(
            List.of(
                Map.of("mensaje", "Insignia desbloqueada", "medio", "EMAIL", "completada", true)));

    Jwt jwt =
        Jwt.withTokenValue("mock-token")
            .header("alg", "none")
            .claim("sub", UUID.randomUUID().toString())
            .claim("personaId", personaId.toString())
            .build();
    JwtAuthenticationToken auth =
        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DONANTE")));

    mvcConDonaciones
        .perform(get("/api/notificaciones/mis-alertas").principal(auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].mensaje").value("Insignia desbloqueada"));
  }

  @Test
  @DisplayName(
      "GET /api/notificaciones/mis-alertas — Retorna 503 estructurado cuando falla NotificacionesGateway")
  void testMisAlertasError503CuandoFallaGateway() throws Exception {
    when(notificacionesGateway.obtenerNotificacionesUsuario("donante@test.com"))
        .thenThrow(new RuntimeException("Timeout en notificaciones"));

    Jwt jwt =
        Jwt.withTokenValue("mock-token")
            .header("alg", "none")
            .claim("sub", UUID.randomUUID().toString())
            .claim("email", "donante@test.com")
            .build();
    JwtAuthenticationToken auth =
        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DONANTE")));

    notificacionesMvc
        .perform(get("/api/notificaciones/mis-alertas").principal(auth))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.error").value("NOTIFICACIONES_UNAVAILABLE"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName(
      "GET /api/notificaciones/mis-alertas — Retorna HTTP 200 con array vacío cuando gateway responde null")
  void testMisAlertasRetornaVacioCuandoGatewayRespondeNull() throws Exception {
    when(notificacionesGateway.obtenerNotificacionesUsuario("donante@test.com")).thenReturn(null);

    Jwt jwt =
        Jwt.withTokenValue("mock-token")
            .header("alg", "none")
            .claim("sub", UUID.randomUUID().toString())
            .claim("email", "donante@test.com")
            .build();
    JwtAuthenticationToken auth =
        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DONANTE")));

    notificacionesMvc
        .perform(get("/api/notificaciones/mis-alertas").principal(auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
  }

  @Test
  @DisplayName(
      "GET /api/notificaciones/mis-alertas — Retorna HTTP 200 con array vacío cuando NotificacionesGateway es null")
  void testMisAlertasRetornaVacioCuandoGatewayEsNull() throws Exception {
    NotificacionesApiController controllerSinGateway = new NotificacionesApiController(null);
    MockMvc mvcSinGateway = MockMvcBuilders.standaloneSetup(controllerSinGateway).build();

    Jwt jwt =
        Jwt.withTokenValue("mock-token")
            .header("alg", "none")
            .claim("sub", UUID.randomUUID().toString())
            .claim("email", "donante@test.com")
            .build();
    JwtAuthenticationToken auth =
        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DONANTE")));

    mvcSinGateway
        .perform(get("/api/notificaciones/mis-alertas").principal(auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
  }

  @Test
  @DisplayName("GET /api/donante/mapa-camiones — Retorna seguimiento de entregas del donante")
  void testMapaCamionesDonante() throws Exception {
    UUID donanteId = UUID.randomUUID();
    when(donacionesGateway.listarDonaciones(any(), any()))
        .thenReturn(
            List.of(
                Map.of(
                    "id", "2418a111-0000-4000-8000-000000000001",
                    "estado", "EN_TRASLADO")));

    Jwt jwt =
        Jwt.withTokenValue("mock-token")
            .header("alg", "none")
            .claim("sub", donanteId.toString())
            .claim("personaId", donanteId.toString())
            .build();
    JwtAuthenticationToken auth =
        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DONANTE")));

    donanteMvc
        .perform(get("/api/donante/mapa-camiones").principal(auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].patenteCamion").exists())
        .andExpect(jsonPath("$[0].choferNombre").exists())
        .andExpect(jsonPath("$[0].lat").exists())
        .andExpect(jsonPath("$[0].lng").exists());
  }

  @Test
  @DisplayName(
      "GET /api/public/mapa-entregas — Retorna centros de acopio, ONGs y entregas públicas")
  void testMapaEntregasPublico() throws Exception {
    when(donacionesGateway.obtenerBeneficiarios()).thenReturn(List.of());

    publicMvc
        .perform(get("/api/public/mapa-entregas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.tipo == 'blue')]").isNotEmpty())
        .andExpect(jsonPath("$[?(@.tipo == 'green')]").isNotEmpty())
        .andExpect(jsonPath("$[?(@.tipo == 'orange')]").isNotEmpty());
  }

  @Test
  @DisplayName("CacheResilienteService — Almacena en caché respuesta fresca del gateway")
  void testCacheResilienteDatosFrescos() {
    Map<String, Object> rutaSimulada = Map.of("id", UUID.randomUUID().toString(), "iniciada", true);
    when(logisticaGateway.listarRutas()).thenReturn(List.of(rutaSimulada));

    List<Map<String, Object>> rutas = cacheResilienteService.obtenerRutasConCache();
    assertNotNull(rutas);
    assertFalse(rutas.isEmpty());

    // Invocación subsiguiente debe obtener de caché sin llamar de nuevo a logisticaGateway
    cacheResilienteService.obtenerRutasConCache();
    org.mockito.Mockito.verify(logisticaGateway, org.mockito.Mockito.times(1)).listarRutas();

    // Invalidar caché debe permitir volver a consultar
    cacheResilienteService.invalidarCache();
    cacheResilienteService.obtenerRutasConCache();
    org.mockito.Mockito.verify(logisticaGateway, org.mockito.Mockito.times(2)).listarRutas();
  }

  @Test
  @DisplayName("CacheResilienteService — Utiliza fallback sanitizado ante timeout de logística")
  void testCacheResilienteFallback() {
    when(logisticaGateway.listarRutas())
        .thenThrow(new RuntimeException("Simulated Render Cold-Start"));

    List<Map<String, Object>> rutas = cacheResilienteService.obtenerRutasConCache();
    assertNotNull(rutas);
    assertFalse(rutas.isEmpty());

    // La segunda invocación debe servirse de la memoria sin reintentar
    List<Map<String, Object>> rutasSegundaVez = cacheResilienteService.obtenerRutasConCache();
    assertNotNull(rutasSegundaVez);
  }
}
