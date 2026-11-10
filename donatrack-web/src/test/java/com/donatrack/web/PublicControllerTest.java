package com.donatrack.web;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.donatrack.web.controller.PublicController;
import com.donatrack.web.dto.TokenResponse;
import com.donatrack.web.dto.UserInfoDto;
import com.donatrack.web.gateway.AuthGateway;
import com.donatrack.web.gateway.IncentivosGateway;
import com.donatrack.web.gateway.LogisticaGateway;
import com.donatrack.web.service.SolicitudOngService;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@DisplayName("PublicController - Tests Unitarios y de Integración MockMvc")
class PublicControllerTest {

  private MockMvc mockMvc;

  private AuthGateway authGateway;
  private IncentivosGateway incentivosGateway;
  private LogisticaGateway logisticaGateway;
  private JwtDecoder jwtDecoder;
  private SolicitudOngService solicitudOngService;

  @BeforeEach
  void setUp() {
    authGateway = mock(AuthGateway.class);
    incentivosGateway = mock(IncentivosGateway.class);
    logisticaGateway = mock(LogisticaGateway.class);
    solicitudOngService = mock(SolicitudOngService.class);
    jwtDecoder = mock(JwtDecoder.class);

    PublicController controller =
        new PublicController(
            authGateway,
            incentivosGateway,
            logisticaGateway,
            jwtDecoder,
            new HttpSessionSecurityContextRepository(),
            solicitudOngService);

    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/templates/");
    viewResolver.setSuffix(".html");

    mockMvc = MockMvcBuilders.standaloneSetup(controller).setViewResolvers(viewResolver).build();
  }

  private Jwt crearJwtSimulado(String username, String rol) {
    return Jwt.withTokenValue("mocked-jwt-token")
        .header("alg", "none")
        .subject(UUID.randomUUID().toString())
        .claim("username", username)
        .claim("roles", List.of(rol))
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();
  }

  // ==========================================
  // VISTAS PÚBLICAS Y HOME
  // ==========================================

  @Nested
  @DisplayName("Vistas Públicas Principales")
  class VistasPublicasTests {

    @Test
    @DisplayName("GET / debe responder 200 OK, cargar vista index y resolver ranking con nombres")
    void testIndexPublicoConRankingResuelto() throws Exception {
      // Arrange
      UUID mariaUuid = UUID.fromString("a1111111-1111-4111-8111-111111111111");
      UUID customUuid = UUID.randomUUID();

      List<Map<String, Object>> top3 =
          List.of(
              Map.of("user", mariaUuid.toString(), "donations", 42, "puntos", 500),
              Map.of("user", customUuid.toString(), "donations", 20, "puntos", 300));
      when(incentivosGateway.obtenerTop3()).thenReturn(top3);

      when(authGateway.listarUsuarios())
          .thenReturn(
              List.of(
                  Map.of(
                      "id", customUuid.toString(),
                      "nombre", "Lucas Solidario",
                      "username", "lsolidario")));

      // Act & Assert
      mockMvc
          .perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(view().name("public/index"))
          .andExpect(model().attributeExists("ranking"))
          .andExpect(model().attribute("ranking", hasSize(2)))
          .andExpect(model().attribute("ranking", hasItem(hasEntry("user", "María González"))))
          .andExpect(model().attribute("ranking", hasItem(hasEntry("user", "Lucas Solidario"))));

      verify(incentivosGateway, times(1)).obtenerTop3();
      verify(authGateway, times(1)).listarUsuarios();
    }

    @Test
    @DisplayName("GET / debe responder 200 OK cuando ranking de incentivos es vacío")
    void testIndexPublicoRankingVacio() throws Exception {
      // Arrange
      when(incentivosGateway.obtenerTop3()).thenReturn(Collections.emptyList());
      when(authGateway.listarUsuarios()).thenReturn(Collections.emptyList());

      // Act & Assert
      mockMvc
          .perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(view().name("public/index"))
          .andExpect(model().attributeExists("ranking"))
          .andExpect(model().attribute("ranking", hasSize(0)));
    }

    @Test
    @DisplayName("GET /login debe mostrar la vista del formulario sin alertas")
    void testLoginPageSinAlertas() throws Exception {
      mockMvc
          .perform(get("/login"))
          .andExpect(status().isOk())
          .andExpect(view().name("public/login"))
          .andExpect(model().attributeDoesNotExist("loginError"))
          .andExpect(model().attributeDoesNotExist("logoutMessage"));
    }

    @Test
    @DisplayName("GET /login?error=true debe inyectar el mensaje de error de autenticación")
    void testLoginPageConError() throws Exception {
      mockMvc
          .perform(get("/login").param("error", "true"))
          .andExpect(status().isOk())
          .andExpect(view().name("public/login"))
          .andExpect(model().attribute("loginError", "Usuario o contraseña inválidos."));
    }

    @Test
    @DisplayName("GET /login?logout=true debe inyectar el mensaje de cierre de sesión exitoso")
    void testLoginPageConLogout() throws Exception {
      mockMvc
          .perform(get("/login").param("logout", "true"))
          .andExpect(status().isOk())
          .andExpect(view().name("public/login"))
          .andExpect(model().attribute("logoutMessage", "Sesión cerrada correctamente."));
    }

    @Test
    @DisplayName("GET /registro debe responder 200 OK y retornar vista public/registro")
    void testRegistroPage() throws Exception {
      mockMvc
          .perform(get("/registro"))
          .andExpect(status().isOk())
          .andExpect(view().name("public/registro"));
    }

    @Test
    @DisplayName("GET /privacidad debe responder 200 OK y retornar vista public/privacidad")
    void testPrivacidadPage() throws Exception {
      mockMvc
          .perform(get("/privacidad"))
          .andExpect(status().isOk())
          .andExpect(view().name("public/privacidad"));
    }

    @Test
    @DisplayName("GET /terminos debe responder 200 OK y retornar vista public/terminos")
    void testTerminosPage() throws Exception {
      mockMvc
          .perform(get("/terminos"))
          .andExpect(status().isOk())
          .andExpect(view().name("public/terminos"));
    }

    @Test
    @DisplayName("GET /healthcheck debe retornar estado UP en formato JSON")
    void testHealthcheck() throws Exception {
      mockMvc
          .perform(get("/healthcheck"))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.status").value("UP"));
    }
  }

  // ==========================================
  // AUTENTICACIÓN Y REGISTRO (POST)
  // ==========================================

  @Nested
  @DisplayName("Autenticación y Registro Local")
  class AuthLocalTests {

    @Test
    @DisplayName(
        "POST /login con credenciales válidas de DONANTE debe redirigir a /app/donante y setear sesión")
    void testLoginExitosoDonante() throws Exception {
      // Arrange
      TokenResponse tokenResp =
          new TokenResponse(
              "dummy-token-donante",
              "bearer",
              3600L,
              new UserInfoDto(
                  UUID.randomUUID(),
                  "donante1",
                  "Donante Uno",
                  "donante@mail.com",
                  "DONANTE",
                  UUID.randomUUID()));

      when(authGateway.login("donante1", "clave123")).thenReturn(tokenResp);
      when(jwtDecoder.decode("dummy-token-donante"))
          .thenReturn(crearJwtSimulado("donante1", "DONANTE"));

      // Act & Assert
      mockMvc
          .perform(post("/login").param("username", "donante1").param("password", "clave123"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/donante"));

      verify(authGateway, times(1)).login("donante1", "clave123");
    }

    @Test
    @DisplayName("POST /login con credenciales válidas de ADMIN debe redirigir a /app/admin")
    void testLoginExitosoAdmin() throws Exception {
      // Arrange
      TokenResponse tokenResp =
          new TokenResponse(
              "dummy-token-admin",
              "bearer",
              3600L,
              new UserInfoDto(
                  UUID.randomUUID(),
                  "admin",
                  "Admin Supremo",
                  "admin@donatrack.org",
                  "ADMIN",
                  UUID.randomUUID()));

      when(authGateway.login("admin", "secret")).thenReturn(tokenResp);
      when(jwtDecoder.decode("dummy-token-admin")).thenReturn(crearJwtSimulado("admin", "ADMIN"));

      // Act & Assert
      mockMvc
          .perform(post("/login").param("username", "admin").param("password", "secret"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin"));
    }

    @Test
    @DisplayName(
        "POST /login con credenciales inválidas debe redirigir a /login con flash attribute loginError")
    void testLoginInvalido() throws Exception {
      // Arrange
      when(authGateway.login("erroneo", "mal")).thenThrow(new RuntimeException("Bad credentials"));

      // Act & Assert
      mockMvc
          .perform(post("/login").param("username", "erroneo").param("password", "mal"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/login"))
          .andExpect(flash().attribute("loginError", "Credenciales inválidas. Reintentá."));
    }

    @Test
    @DisplayName("POST /registro exitoso debe dar de alta al donante y redirigir a /app/donante")
    void testRegistroExitoso() throws Exception {
      // Arrange
      TokenResponse tokenResp =
          new TokenResponse(
              "dummy-token-new",
              "bearer",
              3600L,
              new UserInfoDto(
                  UUID.randomUUID(),
                  "nuevo",
                  "Nuevo Donante",
                  "nuevo@mail.com",
                  "DONANTE",
                  UUID.randomUUID()));

      when(authGateway.registrar(anyMap())).thenReturn(tokenResp);
      when(jwtDecoder.decode("dummy-token-new")).thenReturn(crearJwtSimulado("nuevo", "DONANTE"));

      // Act & Assert
      mockMvc
          .perform(
              post("/registro")
                  .param("nombre", "Nuevo Donante")
                  .param("username", "nuevo")
                  .param("email", "nuevo@mail.com")
                  .param("password", "clave123"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/donante"));

      verify(authGateway, times(1)).registrar(anyMap());
    }

    @Test
    @DisplayName(
        "POST /registro con rol BENEFICIARIA debe registrar como BENEFICIARIA_PENDIENTE y redirigir a /login sin login directo")
    void testRegistroBeneficiariaPendiente() throws Exception {
      mockMvc
          .perform(
              post("/registro")
                  .param("nombre", "Comedor Solidario")
                  .param("username", "comedorsolidario")
                  .param("email", "contacto@comedorsolidario.org")
                  .param("password", "clave123")
                  .param("rol", "BENEFICIARIA")
                  .param("cuit", "30-11223344-5")
                  .param("direccion", "Av. Caseros 1234, CABA"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/login"))
          .andExpect(flash().attributeExists("registerSuccess"));

      verify(authGateway, times(1))
          .registrar(
              org.mockito.ArgumentMatchers.argThat(
                  m -> "BENEFICIARIA_PENDIENTE".equals(m.get("rol"))));
    }

    @Test
    @DisplayName(
        "POST /registro con fallo en gateway debe redirigir a /registro con flash registerError")
    void testRegistroFallido() throws Exception {
      // Arrange
      when(authGateway.registrar(anyMap()))
          .thenThrow(new RuntimeException("El nombre de usuario ya está registrado"));

      // Act & Assert
      mockMvc
          .perform(
              post("/registro")
                  .param("nombre", "Duplicado")
                  .param("username", "duplicado")
                  .param("email", "duplicado@mail.com")
                  .param("password", "clave123"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/registro"))
          .andExpect(
              flash()
                  .attribute(
                      "registerError",
                      containsString(
                          "No se pudo crear la cuenta: El nombre de usuario ya está registrado")));
    }
  }

  // ==========================================
  // API PÚBLICA DE RASTREO DE REMITOS
  // ==========================================

  @Nested
  @DisplayName("API Pública: Rastreo de Remitos")
  class RastreoRemitoTests {

    @Test
    @DisplayName("GET /api/public/rastreo sin parámetro código debe responder 400 Bad Request")
    void testRastreoCodigoVacio() throws Exception {
      mockMvc
          .perform(get("/api/public/rastreo"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error").value("Ingresá un código de remito o entrega."));
    }

    @Test
    @DisplayName(
        "GET /api/public/rastreo con remito existente debe retornar datos de entrega y fotos sanitizadas")
    void testRastreoRemitoExistente() throws Exception {
      // Arrange
      Map<String, Object> parada =
          Map.of(
              "direccion",
                  Map.of(
                      "calle", "Av. San Martín",
                      "altura", "1250",
                      "localidad", "Quilmes"),
              "entregas",
                  List.of(
                      Map.of(
                          "idEntrega",
                          "3300-ABCD-1234",
                          "estado",
                          "ENTREGADA",
                          "pesoEstimado",
                          120.0,
                          "volumenEstimado",
                          1.2,
                          "comprobanteRecepcion",
                          Map.of(
                              "fotos", List.of("https://storage.donatrack.org/remito-3300.jpg")))));

      Map<String, Object> ruta =
          Map.of(
              "camion", Map.of("patente", "AF456CD"),
              "chofer", Map.of("nombre", "Carlos López"),
              "fechaOperativa", "2026-11-09",
              "paradas", List.of(parada));

      when(logisticaGateway.listarRutas()).thenReturn(List.of(ruta));

      // Act & Assert
      mockMvc
          .perform(get("/api/public/rastreo").param("codigo", "#DT-3300"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.codigo").value("#DT-3300"))
          .andExpect(jsonPath("$.estado").value("ENTREGADA"))
          .andExpect(jsonPath("$.patente").value("AF456CD"))
          .andExpect(jsonPath("$.chofer").value("Carlos López"))
          .andExpect(jsonPath("$.direccion").value("Av. San Martín 1250, Quilmes"))
          .andExpect(
              jsonPath("$.comprobante.fotos[0]")
                  .value(
                      "https://nkqyubnzckxojsubyiuk.supabase.co/storage/v1/object/public/remitos/remito-3300.webp"));
    }

    @Test
    @DisplayName("GET /api/public/rastreo buscando por patente de camión debe encontrar la entrega")
    void testRastreoPorPatente() throws Exception {
      // Arrange
      Map<String, Object> parada =
          Map.of(
              "direccion", Map.of("calle", "Rivadavia", "altura", "500", "localidad", "CABA"),
              "entregas",
                  List.of(
                      Map.of(
                          "idEntrega",
                          "3200-BXYZ",
                          "estado",
                          "EN_CAMINO",
                          "pesoEstimado",
                          50.0,
                          "volumenEstimado",
                          0.6)));

      Map<String, Object> ruta =
          Map.of(
              "camion", Map.of("patente", "AA999ZZ"),
              "chofer", Map.of("nombre", "Martín Palermo"),
              "paradas", List.of(parada));

      when(logisticaGateway.listarRutas()).thenReturn(List.of(ruta));

      // Act & Assert
      mockMvc
          .perform(get("/api/public/rastreo").param("codigo", "AA999ZZ"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.patente").value("AA999ZZ"))
          .andExpect(jsonPath("$.estado").value("EN_CAMINO"));
    }

    @Test
    @DisplayName(
        "GET /api/public/rastreo con código con espacios y minúsculas debe sanitizar y encontrar")
    void testRastreoConEspaciosYMinusculas() throws Exception {
      // Arrange
      Map<String, Object> parada =
          Map.of(
              "direccion", Map.of("calle", "Belgrano", "altura", "100", "localidad", "CABA"),
              "entregas",
                  List.of(
                      Map.of(
                          "idEntrega", "3300-ENTREGA",
                          "estado", "ENTREGADA")));

      Map<String, Object> ruta =
          Map.of(
              "camion", Map.of("patente", "AA123BB"),
              "chofer", Map.of("nombre", "Chofer Test"),
              "paradas", List.of(parada));

      when(logisticaGateway.listarRutas()).thenReturn(List.of(ruta));

      // Act & Assert
      mockMvc
          .perform(get("/api/public/rastreo").param("codigo", "  #dt-3300  "))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.codigo").value("#DT-3300"))
          .andExpect(jsonPath("$.estado").value("ENTREGADA"));
    }

    @Test
    @DisplayName("GET /api/public/rastreo con código inexistente debe responder 404 Not Found")
    void testRastreoNoEncontrado() throws Exception {
      // Arrange
      when(logisticaGateway.listarRutas()).thenReturn(Collections.emptyList());

      // Act & Assert
      mockMvc
          .perform(get("/api/public/rastreo").param("codigo", "DT-9999"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.error", containsString("No se encontró el remito DT-9999")));
    }
  }

  // ==========================================
  // CALCULADOR DE IMPACTO CÍVICO
  // ==========================================

  @Nested
  @DisplayName("API Pública: Calculador de Impacto Cívico")
  class CalculadorImpactoTests {

    @ParameterizedTest(name = "Categoría {0} con {1} unidades debe calcular métricas esperadas")
    @CsvSource({
      "alimentos, 10, Alimentos no Perecederos, 40 raciones nutritivas preparadas, +100 puntos cívicos",
      "abrigo, 10, Ropa de Abrigo e Indumentaria, 10 personas protegidas del frío, +150 puntos cívicos",
      "ropa, 20, Ropa de Abrigo e Indumentaria, 20 personas protegidas del frío, +300 puntos cívicos",
      "utiles, 10, Mobiliario y Útiles Escolares, 20 alumnos con pupitre y útiles todo el año, +250 puntos cívicos",
      "escolar, 5, Mobiliario y Útiles Escolares, 10 alumnos con pupitre y útiles todo el año, +125 puntos cívicos"
    })
    void testCalculadorImpactoParametrizado(
        String categoria,
        double cantidad,
        String categoriaEsperada,
        String impactoPrincipalEsperado,
        String puntosEsperados)
        throws Exception {
      mockMvc
          .perform(
              get("/api/public/calculador-impacto")
                  .param("categoria", categoria)
                  .param("cantidad", String.valueOf(cantidad)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.categoria").value(categoriaEsperada))
          .andExpect(jsonPath("$.impactoPrincipal").value(impactoPrincipalEsperado))
          .andExpect(jsonPath("$.puntos").value(puntosEsperados));
    }

    @Test
    @DisplayName("Calculador de impacto debe normalizar cantidad menor o igual a cero a 1")
    void testCalculadorCantidadMinima() throws Exception {
      mockMvc
          .perform(
              get("/api/public/calculador-impacto")
                  .param("categoria", "alimentos")
                  .param("cantidad", "-5"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.impactoPrincipal").value("4 raciones nutritivas preparadas"))
          .andExpect(jsonPath("$.puntos").value("+10 puntos cívicos"));
    }
  }

  // ==========================================
  // COMPROBANTES DIGITALES SVG (/remitos y /fotos)
  // ==========================================

  @Nested
  @DisplayName("Generador de Remito SVG Digital")
  class RemitoSvgTests {

    @ParameterizedTest(name = "Endpoint {0} debe devolver SVG firmado")
    @ValueSource(strings = {"/remitos/DT-3300.jpg", "/fotos/DT-3300.webp", "/remitos/3300.png"})
    void testObtenerRemitoSvg(String uri) throws Exception {
      mockMvc
          .perform(get(uri))
          .andExpect(status().isOk())
          .andExpect(content().contentType("image/svg+xml;charset=UTF-8"))
          .andExpect(content().string(containsString("<svg")))
          .andExpect(content().string(containsString("DonaTrack • Comprobante Digital de Entrega")))
          .andExpect(content().string(containsString("CERTIFICADO #DT-3300")))
          .andExpect(content().string(containsString("ENTREGA VERIFICADA")));
    }
  }

  // ==========================================
  // CHATBOT PROXY FALLBACK
  // ==========================================

  @Nested
  @DisplayName("Chatbot Proxy")
  class ChatbotProxyTests {

    @Test
    @DisplayName("POST /api/public/chatbot con webhook n8n inaccesible responde fallback 200 OK")
    void testChatbotProxyFallback() throws Exception {
      mockMvc
          .perform(
              post("/api/public/chatbot")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"mensaje\":\"Hola, cómo puedo donar?\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.respuesta", containsString("Hola! En este momento")));
    }
  }

  // ==========================================
  // POSTULACIÓN SOLICITUDES ONG
  // ==========================================

  @Nested
  @DisplayName("Solicitudes de Adhesión para ONGs")
  class SolicitudesOngTests {

    @Test
    @DisplayName("POST /api/public/solicitudes-ong registra postulación exitosamente")
    void testRegistrarSolicitudOngExitoso() throws Exception {
      // Arrange
      SolicitudOngService.SolicitudOng solicitud =
          new SolicitudOngService.SolicitudOng(
              "ONG-1234",
              "Comedor Los Piletones",
              "30-71112222-9",
              "Av. Lacarra 3400",
              "contacto@piletones.org",
              "+54 11 4455-6677",
              "Margarita Barrientos",
              "Comedor",
              "Alimentos no perecederos",
              "PENDIENTE",
              "2026-11-09 10:00");

      when(solicitudOngService.registrarSolicitud(anyMap())).thenReturn(solicitud);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/public/solicitudes-ong")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "nombreOng": "Comedor Los Piletones",
                        "cuit": "30-71112222-9",
                        "email": "contacto@piletones.org"
                      }
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.ok").value(true))
          .andExpect(jsonPath("$.id").value("ONG-1234"))
          .andExpect(jsonPath("$.mensaje", containsString("Solicitud registrada con éxito")));
    }

    @Test
    @DisplayName("POST /api/public/solicitudes-ong con error de servicio responde 400 Bad Request")
    void testRegistrarSolicitudOngError() throws Exception {
      // Arrange
      when(solicitudOngService.registrarSolicitud(anyMap()))
          .thenThrow(new IllegalArgumentException("El CUIT ingresado es inválido"));

      // Act & Assert
      mockMvc
          .perform(
              post("/api/public/solicitudes-ong")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"cuit\":\"invalido\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error").value("El CUIT ingresado es inválido"));
    }
  }

  // ==========================================
  // SSO / SOCIAL LOGIN
  // ==========================================

  @Nested
  @DisplayName("Autenticación SSO y Social Login")
  class SocialLoginTests {

    @Test
    @DisplayName(
        "GET /oauth2/authorization/{provider}?mock=true con código válido debe completar SSO a /app/donante")
    void testSocialLoginMockExitoso() throws Exception {
      // Arrange
      when(authGateway.obtenerCodigoSocial("google")).thenReturn("code-mock-999");

      TokenResponse tokenResp =
          new TokenResponse(
              "sso-token-abc",
              "bearer",
              3600L,
              new UserInfoDto(
                  UUID.randomUUID(),
                  "google_user",
                  "Google User",
                  "user@gmail.com",
                  "DONANTE",
                  UUID.randomUUID()));
      when(authGateway.exchangeToken("code-mock-999")).thenReturn(tokenResp);
      when(jwtDecoder.decode("sso-token-abc"))
          .thenReturn(crearJwtSimulado("google_user", "DONANTE"));

      // Act & Assert
      mockMvc
          .perform(get("/oauth2/authorization/google").param("mock", "true"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/donante"));
    }

    @Test
    @DisplayName(
        "GET /oauth2/authorization/{provider}?mock=true cuando falla código debe redirigir a /login con flash error")
    void testSocialLoginMockFallo() throws Exception {
      // Arrange
      when(authGateway.obtenerCodigoSocial("github")).thenReturn(null);

      // Act & Assert
      mockMvc
          .perform(get("/oauth2/authorization/github").param("mock", "true"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/login"))
          .andExpect(flash().attribute("loginError", "No se pudo iniciar sesión con github."));
    }

    @Test
    @DisplayName("GET /auth/callback con código inválido redirige a /login con flash error")
    void testCallbackSsoError() throws Exception {
      // Arrange
      when(authGateway.exchangeToken("bad-code"))
          .thenThrow(new RuntimeException("Token exchange failed"));

      // Act & Assert
      mockMvc
          .perform(get("/auth/callback").param("code", "bad-code"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/login"))
          .andExpect(
              flash().attribute("loginError", "No se pudo completar el inicio de sesión social."));
    }
  }
}
