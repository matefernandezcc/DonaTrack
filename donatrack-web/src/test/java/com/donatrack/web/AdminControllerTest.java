package com.donatrack.web;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.donatrack.web.controller.AdminController;
import com.donatrack.web.dto.CamionDto;
import com.donatrack.web.dto.ChoferDto;
import com.donatrack.web.gateway.AuthGateway;
import com.donatrack.web.gateway.DonacionesGateway;
import com.donatrack.web.gateway.IncentivosGateway;
import com.donatrack.web.gateway.LogisticaGateway;
import com.donatrack.web.service.SolicitudOngService;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@DisplayName("AdminController - Tests de Administración MockMvc")
class AdminControllerTest {

  private MockMvc mockMvc;

  private LogisticaGateway logisticaGateway;
  private IncentivosGateway incentivosGateway;
  private DonacionesGateway donacionesGateway;
  private AuthGateway authGateway;
  private SolicitudOngService solicitudOngService;

  private JwtAuthenticationToken adminAuth;
  private JwtAuthenticationToken donanteAuth;
  private JwtAuthenticationToken benefAuth;

  @BeforeEach
  void setUp() {
    logisticaGateway = mock(LogisticaGateway.class);
    incentivosGateway = mock(IncentivosGateway.class);
    donacionesGateway = mock(DonacionesGateway.class);
    authGateway = mock(AuthGateway.class);
    solicitudOngService = mock(SolicitudOngService.class);

    AdminController controller =
        new AdminController(
            logisticaGateway,
            incentivosGateway,
            donacionesGateway,
            authGateway,
            solicitudOngService);

    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/templates/");
    viewResolver.setSuffix(".html");

    mockMvc = MockMvcBuilders.standaloneSetup(controller).setViewResolvers(viewResolver).build();

    // Tokens de autenticación para distintos roles
    Jwt jwtAdmin =
        Jwt.withTokenValue("mock-jwt-admin")
            .header("alg", "none")
            .subject(UUID.randomUUID().toString())
            .claim("name", "Admin DonaTrack")
            .claim("username", "admin")
            .claim("roles", List.of("ADMIN"))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
    adminAuth =
        new JwtAuthenticationToken(jwtAdmin, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    Jwt jwtDonante =
        Jwt.withTokenValue("mock-jwt-donante")
            .header("alg", "none")
            .subject(UUID.randomUUID().toString())
            .claim("name", "Carlos Donante")
            .claim("username", "cdonante")
            .claim("roles", List.of("DONANTE"))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
    donanteAuth =
        new JwtAuthenticationToken(jwtDonante, List.of(new SimpleGrantedAuthority("ROLE_DONANTE")));

    Jwt jwtBenef =
        Jwt.withTokenValue("mock-jwt-benef")
            .header("alg", "none")
            .subject(UUID.randomUUID().toString())
            .claim("name", "Fundación Esperanza")
            .claim("username", "esperanza")
            .claim("roles", List.of("BENEFICIARIA"))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
    benefAuth =
        new JwtAuthenticationToken(
            jwtBenef, List.of(new SimpleGrantedAuthority("ROLE_BENEFICIARIA")));
  }

  // ==========================================
  // DASHBOARD ROOT & PERFIL
  // ==========================================

  @Nested
  @DisplayName("Navegación Raíz y Perfil de Usuario")
  class DashboardRootTests {

    @Test
    @DisplayName("GET /app con ROLE_ADMIN debe redirigir a /app/admin")
    void testRootRedirigeAdmin() throws Exception {
      mockMvc
          .perform(get("/app").principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin"));
    }

    @Test
    @DisplayName("GET /app con ROLE_DONANTE debe redirigir a /app/donante")
    void testRootRedirigeDonante() throws Exception {
      mockMvc
          .perform(get("/app").principal(donanteAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/donante"));
    }

    @Test
    @DisplayName("GET /app con ROLE_BENEFICIARIA debe redirigir a /app/beneficiaria")
    void testRootRedirigeBeneficiaria() throws Exception {
      mockMvc
          .perform(get("/app").principal(benefAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/beneficiaria"));
    }

    @Test
    @DisplayName(
        "POST /app/perfil/actualizar actualiza descripción y redirige al referer con flashOk")
    void testActualizarPerfilExitoso() throws Exception {
      // Act & Assert
      mockMvc
          .perform(
              post("/app/perfil/actualizar")
                  .principal(adminAuth)
                  .header("Referer", "/app/admin")
                  .param("descripcion", "Nueva descripción institucional")
                  .param("ubicacion", "Córdoba, Argentina"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin"))
          .andExpect(flash().attribute("flashOk", "Perfil actualizado con éxito."));

      verify(authGateway, times(1))
          .actualizarPerfil(
              any(UUID.class), eq("Nueva descripción institucional"), eq("Córdoba, Argentina"));
    }

    @Test
    @DisplayName("POST /app/perfil/actualizar con error en gateway setea flashError")
    void testActualizarPerfilError() throws Exception {
      doThrow(new RuntimeException("Servicio no disponible"))
          .when(authGateway)
          .actualizarPerfil(any(), any(), any());

      mockMvc
          .perform(
              post("/app/perfil/actualizar")
                  .principal(adminAuth)
                  .param("descripcion", "Desc")
                  .param("ubicacion", "Ubi"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/donante"))
          .andExpect(
              flash()
                  .attribute(
                      "flashError",
                      containsString("Error al actualizar perfil: Servicio no disponible")));
    }

    @Test
    @DisplayName(
        "populateUserAttributes desacoplado: obtiene atributos de usuario desde claims de JWT sin invocar AuthGateway")
    void populateUserAttributes_fromJwtClaims_withoutAuthGatewayCall() throws Exception {
      // Arrange
      Jwt jwtCustom =
          Jwt.withTokenValue("mock-jwt-custom")
              .header("alg", "none")
              .subject(UUID.randomUUID().toString())
              .claim("name", "Lorena Gomez")
              .claim("username", "lgomez")
              .claim("roles", List.of("ADMIN"))
              .claim("descripcion", "Coordinadora de Distribución Central")
              .claim("ubicacion", "Rosario, Santa Fe")
              .issuedAt(Instant.now())
              .expiresAt(Instant.now().plusSeconds(3600))
              .build();
      JwtAuthenticationToken customAuth =
          new JwtAuthenticationToken(
              jwtCustom, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

      when(logisticaGateway.listarCamiones()).thenReturn(Collections.emptyList());
      when(logisticaGateway.listarChoferes()).thenReturn(Collections.emptyList());
      when(logisticaGateway.listarRutas()).thenReturn(Collections.emptyList());

      // Act & Assert
      mockMvc
          .perform(get("/app/admin").principal(customAuth))
          .andExpect(status().isOk())
          .andExpect(model().attribute("nombreUsuario", "lgomez"))
          .andExpect(model().attribute("descripcion", "Coordinadora de Distribución Central"))
          .andExpect(model().attribute("ubicacion", "Rosario, Santa Fe"));

      verifyNoInteractions(authGateway);
    }

    @Test
    @DisplayName(
        "POST /app/perfil/actualizar persiste en HttpSession los atributos actualizados de perfil")
    void actualizarPerfil_persisteEnHttpSession() throws Exception {
      // Arrange
      MockHttpSession session = new MockHttpSession();
      String nuevaDescripcion = "Logística humanitaria y coordinación de voluntarios";
      String nuevaUbicacion = "Córdoba Capital, Argentina";

      // Act & Assert
      mockMvc
          .perform(
              post("/app/perfil/actualizar")
                  .principal(adminAuth)
                  .session(session)
                  .header("Referer", "/app/admin")
                  .param("descripcion", nuevaDescripcion)
                  .param("ubicacion", nuevaUbicacion))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin"))
          .andExpect(flash().attribute("flashOk", "Perfil actualizado con éxito."));

      assertEquals(nuevaDescripcion, session.getAttribute("perfil_descripcion"));
      assertEquals(nuevaUbicacion, session.getAttribute("perfil_ubicacion"));

      verify(authGateway, times(1))
          .actualizarPerfil(any(UUID.class), eq(nuevaDescripcion), eq(nuevaUbicacion));
    }
  }

  // ==========================================
  // PANEL RESUMEN ADMINISTRACIÓN
  // ==========================================

  @Nested
  @DisplayName("Resumen de Operaciones")
  class AdminResumenTests {

    @Test
    @DisplayName("GET /app/admin debe retornar 200 OK con contadores de flota y rutas")
    void testAdminResumen() throws Exception {
      // Arrange
      when(logisticaGateway.listarCamiones())
          .thenReturn(List.of(new CamionDto("AA111BB", 20.0, 3.0, 4000.0)));
      when(logisticaGateway.listarChoferes())
          .thenReturn(List.of(new ChoferDto("CH-01", "Mario", "Bros", "+54111")));
      when(logisticaGateway.listarRutas())
          .thenReturn(List.of(Map.of("id", UUID.randomUUID()), Map.of("id", UUID.randomUUID())));

      // Act & Assert
      mockMvc
          .perform(get("/app/admin").principal(adminAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/admin/resumen"))
          .andExpect(model().attribute("totalCamiones", 1))
          .andExpect(model().attribute("totalChoferes", 1))
          .andExpect(model().attribute("totalRutas", 2));
    }
  }

  // ==========================================
  // GESTIÓN DE CAMIONES
  // ==========================================

  @Nested
  @DisplayName("Gestión de Camiones")
  class GestionCamionesTests {

    @Test
    @DisplayName("GET /app/admin/camiones debe retornar lista de camiones y DTO nuevoCamion")
    void testListarCamiones() throws Exception {
      when(logisticaGateway.listarCamiones())
          .thenReturn(List.of(new CamionDto("AB123CD", 25.0, 3.5, 4500.0)));

      mockMvc
          .perform(get("/app/admin/camiones").principal(adminAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/admin/camiones"))
          .andExpect(model().attributeExists("camiones"))
          .andExpect(model().attributeExists("nuevoCamion"));
    }

    @Test
    @DisplayName("POST /app/admin/camiones exitoso debe invocar gateway y redirigir con flashOk")
    void testCrearCamionExitoso() throws Exception {
      mockMvc
          .perform(
              post("/app/admin/camiones")
                  .principal(adminAuth)
                  .param("patente", "AE789XY")
                  .param("capacidadVolumen", "35.0")
                  .param("altura", "4.0")
                  .param("capacidadCarga", "6000.0"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/camiones"))
          .andExpect(flash().attribute("flashOk", "Camión AE789XY registrado con éxito."));

      verify(logisticaGateway, times(1)).crearCamion(new CamionDto("AE789XY", 35.0, 4.0, 6000.0));
    }

    @Test
    @DisplayName(
        "POST /app/admin/camiones con error en gateway debe atrapar excepción y setear flashError")
    void testCrearCamionError() throws Exception {
      doThrow(new RuntimeException("Patente ya existente"))
          .when(logisticaGateway)
          .crearCamion(any());

      mockMvc
          .perform(
              post("/app/admin/camiones")
                  .principal(adminAuth)
                  .param("patente", "DUPLICADA")
                  .param("capacidadVolumen", "20.0")
                  .param("altura", "3.0")
                  .param("capacidadCarga", "4000.0"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/camiones"))
          .andExpect(flash().attribute("flashError", containsString("Patente ya existente")));
    }

    @Test
    @DisplayName("POST /app/admin/camiones/{patente}/eliminar exitoso redirige con flashOk")
    void testEliminarCamionExitoso() throws Exception {
      mockMvc
          .perform(post("/app/admin/camiones/AE789XY/eliminar").principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/camiones"))
          .andExpect(flash().attribute("flashOk", "Camión AE789XY eliminado."));

      verify(logisticaGateway, times(1)).eliminarCamion("AE789XY");
    }

    @Test
    @DisplayName("POST /app/admin/camiones/{patente}/eliminar con error redirige con flashError")
    void testEliminarCamionError() throws Exception {
      doThrow(new RuntimeException("Camión asignado a una ruta activa"))
          .when(logisticaGateway)
          .eliminarCamion("AE789XY");

      mockMvc
          .perform(post("/app/admin/camiones/AE789XY/eliminar").principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/camiones"))
          .andExpect(
              flash().attribute("flashError", containsString("Camión asignado a una ruta activa")));
    }
  }

  // ==========================================
  // GESTIÓN DE CHOFERES
  // ==========================================

  @Nested
  @DisplayName("Gestión de Choferes")
  class GestionChoferesTests {

    @Test
    @DisplayName("GET /app/admin/choferes debe retornar lista de choferes y DTO nuevoChofer")
    void testListarChoferes() throws Exception {
      when(logisticaGateway.listarChoferes())
          .thenReturn(List.of(new ChoferDto("CH-100", "Romina", "Pérez", "+54 11 1234-5678")));

      mockMvc
          .perform(get("/app/admin/choferes").principal(adminAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/admin/choferes"))
          .andExpect(model().attributeExists("choferes"))
          .andExpect(model().attributeExists("nuevoChofer"));
    }

    @Test
    @DisplayName("POST /app/admin/choferes exitoso debe registrar chofer y redirigir con flashOk")
    void testCrearChoferExitoso() throws Exception {
      mockMvc
          .perform(
              post("/app/admin/choferes")
                  .principal(adminAuth)
                  .param("legajo", "CH-505")
                  .param("nombre", "Esteban")
                  .param("apellido", "Quito")
                  .param("telefono", "+54 11 9988-7766"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/choferes"))
          .andExpect(flash().attribute("flashOk", "Chofer Esteban Quito registrado con éxito."));

      verify(logisticaGateway, times(1))
          .crearChofer(new ChoferDto("CH-505", "Esteban", "Quito", "+54 11 9988-7766"));
    }

    @Test
    @DisplayName("POST /app/admin/choferes con error en gateway setea flashError")
    void testCrearChoferError() throws Exception {
      doThrow(new RuntimeException("Legajo duplicado")).when(logisticaGateway).crearChofer(any());

      mockMvc
          .perform(
              post("/app/admin/choferes")
                  .principal(adminAuth)
                  .param("legajo", "CH-505")
                  .param("nombre", "Esteban")
                  .param("apellido", "Quito")
                  .param("telefono", "+54 11 9988-7766"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/choferes"))
          .andExpect(flash().attribute("flashError", containsString("Legajo duplicado")));
    }

    @Test
    @DisplayName("POST /app/admin/choferes/{legajo}/eliminar exitoso redirige con flashOk")
    void testEliminarChoferExitoso() throws Exception {
      mockMvc
          .perform(post("/app/admin/choferes/CH-505/eliminar").principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/choferes"))
          .andExpect(flash().attribute("flashOk", "Chofer con legajo CH-505 eliminado."));

      verify(logisticaGateway, times(1)).eliminarChofer("CH-505");
    }

    @Test
    @DisplayName("POST /app/admin/choferes/{legajo}/eliminar con error redirige con flashError")
    void testEliminarChoferError() throws Exception {
      doThrow(new RuntimeException("Chofer con ruta en ejecución"))
          .when(logisticaGateway)
          .eliminarChofer("CH-505");

      mockMvc
          .perform(post("/app/admin/choferes/CH-505/eliminar").principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/choferes"))
          .andExpect(
              flash().attribute("flashError", containsString("Chofer con ruta en ejecución")));
    }
  }

  // ==========================================
  // RUTAS Y RANKING ADMIN
  // ==========================================

  @Nested
  @DisplayName("Rutas y Ranking de Incentivos")
  class RutasYRankingTests {

    @Test
    @DisplayName("GET /app/admin/rutas debe responder 200 OK y retornar lista de rutas")
    void testListarRutas() throws Exception {
      when(logisticaGateway.listarRutas())
          .thenReturn(
              List.of(
                  Map.of(
                      "id", UUID.randomUUID(),
                      "fechaOperativa", "2026-11-10",
                      "estado", "PLANIFICADA")));

      mockMvc
          .perform(get("/app/admin/rutas").principal(adminAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/admin/rutas"))
          .andExpect(model().attributeExists("rutas"));
    }

    @Test
    @DisplayName("GET /app/admin/ranking resuelve nombres legibles de usuarios y donaciones")
    void testAdminRankingConNombres() throws Exception {
      // Arrange
      UUID userUuid = UUID.randomUUID();
      when(incentivosGateway.obtenerTop3())
          .thenReturn(
              List.of(
                  Map.of("user", userUuid.toString(), "totalDonations", 15),
                  Map.of("user", "a1111111-1111-4111-8111-111111111111", "totalDonations", 25)));

      when(authGateway.listarUsuarios())
          .thenReturn(List.of(Map.of("id", userUuid.toString(), "nombre", "Martín Palermo")));

      when(donacionesGateway.obtenerPersonas()).thenReturn(Collections.emptyList());

      // Act & Assert
      mockMvc
          .perform(get("/app/admin/ranking").principal(adminAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/admin/ranking"))
          .andExpect(model().attributeExists("ranking"))
          .andExpect(model().attribute("ranking", hasSize(2)))
          .andExpect(model().attribute("ranking", hasItem(hasEntry("nombre", "Martín Palermo"))))
          .andExpect(model().attribute("ranking", hasItem(hasEntry("nombre", "María González"))));
    }
  }

  // ==========================================
  // MATCHMAKING DE DONACIONES
  // ==========================================

  @Nested
  @DisplayName("Motor de Matchmaking Cívico")
  class MatchmakingTests {

    @Test
    @DisplayName(
        "GET /app/admin/matchmaking sin donacionId debe mostrar bandeja de donaciones y beneficiarios")
    void testMatchmakingBandejaPrincipal() throws Exception {
      // Arrange
      when(donacionesGateway.obtenerBeneficiarios())
          .thenReturn(
              List.of(
                  Map.of(
                      "id", UUID.randomUUID(),
                      "correoRepresentante", "comedor@esperanza.org",
                      "necesidadesDeclaradas", List.of())));

      when(donacionesGateway.listarDonaciones(null, null))
          .thenReturn(
              List.of(
                  Map.of(
                      "id", UUID.randomUUID(),
                      "estado", "EN_DEPOSITO",
                      "descripcion", "Arroz y Fideos"),
                  Map.of(
                      "id", UUID.randomUUID(),
                      "estado", "ASIGNADA",
                      "correoBeneficiario", "comedor@esperanza.org")));

      // Act & Assert
      mockMvc
          .perform(get("/app/admin/matchmaking").principal(adminAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/admin/matchmaking"))
          .andExpect(model().attributeExists("beneficiarios"))
          .andExpect(model().attributeExists("donacionesPendientes"))
          .andExpect(model().attributeExists("donacionesAsignadas"))
          .andExpect(model().attribute("algoritmoSeleccionado", "HIBRIDO"));
    }

    @Test
    @DisplayName(
        "GET /app/admin/matchmaking con donacionId y algoritmo SEMANTICO debe obtener sugerencias enriquecidas")
    void testMatchmakingConDonacionYSugerencias() throws Exception {
      // Arrange
      UUID donacionId = UUID.randomUUID();
      UUID benefId = UUID.randomUUID();

      when(donacionesGateway.obtenerBeneficiarios())
          .thenReturn(
              List.of(
                  Map.of(
                      "id", benefId.toString(),
                      "correoRepresentante", "coord@comedor.org",
                      "necesidadesDeclaradas", List.of())));

      when(donacionesGateway.listarDonaciones(null, null))
          .thenReturn(List.of(Map.of("id", donacionId.toString(), "estado", "EN_DEPOSITO")));

      when(donacionesGateway.sugerirBeneficiarios(donacionId, "SEMANTICO"))
          .thenReturn(List.of(Map.of("id", benefId.toString(), "compatibilidad", 0.95)));

      // Act & Assert
      mockMvc
          .perform(
              get("/app/admin/matchmaking")
                  .principal(adminAuth)
                  .param("donacionId", donacionId.toString())
                  .param("algoritmo", "SEMANTICO"))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/admin/matchmaking"))
          .andExpect(model().attributeExists("sugerencias"))
          .andExpect(model().attribute("algoritmoSeleccionado", "SEMANTICO"))
          .andExpect(model().attributeExists("donacionSeleccionada"));
    }

    @Test
    @DisplayName(
        "POST /app/admin/matchmaking/asignar exitoso debe invocar donacionesGateway y setear flashOk")
    void testAsignarDonacionExitoso() throws Exception {
      UUID donacionId = UUID.randomUUID();
      UUID benefId = UUID.randomUUID();

      mockMvc
          .perform(
              post("/app/admin/matchmaking/asignar")
                  .principal(adminAuth)
                  .param("donacionId", donacionId.toString())
                  .param("beneficiarioId", benefId.toString()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/matchmaking"))
          .andExpect(flash().attributeExists("flashOk"));

      verify(donacionesGateway, times(1)).asignarDonacion(donacionId, benefId);
    }

    @Test
    @DisplayName("POST /app/admin/matchmaking/asignar con error setea flashError")
    void testAsignarDonacionError() throws Exception {
      UUID donacionId = UUID.randomUUID();
      UUID benefId = UUID.randomUUID();

      doThrow(new RuntimeException("Donación ya fue asignada"))
          .when(donacionesGateway)
          .asignarDonacion(donacionId, benefId);

      mockMvc
          .perform(
              post("/app/admin/matchmaking/asignar")
                  .principal(adminAuth)
                  .param("donacionId", donacionId.toString())
                  .param("beneficiarioId", benefId.toString()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/matchmaking"))
          .andExpect(flash().attribute("flashError", containsString("Donación ya fue asignada")));
    }

    @Test
    @DisplayName(
        "POST /app/admin/matchmaking/batch exitoso ejecuta batch sobre donaciones pendientes")
    void testBatchMatchmakingExitoso() throws Exception {
      mockMvc
          .perform(post("/app/admin/matchmaking/batch").principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/matchmaking"))
          .andExpect(flash().attributeExists("flashOk"));

      verify(donacionesGateway, times(1)).ejecutarMatchmakingBatch();
    }

    @Test
    @DisplayName("POST /app/admin/matchmaking/batch con error en gateway setea flashError")
    void testBatchMatchmakingError() throws Exception {
      doThrow(new RuntimeException("Fallo en optimizador genético"))
          .when(donacionesGateway)
          .ejecutarMatchmakingBatch();

      mockMvc
          .perform(post("/app/admin/matchmaking/batch").principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/matchmaking"))
          .andExpect(
              flash().attribute("flashError", containsString("Fallo en optimizador genético")));
    }
  }

  // ==========================================
  // CARGA MASIVA CSV
  // ==========================================

  @Nested
  @DisplayName("Importación Masiva CSV")
  class CsvImportTests {

    @Test
    @DisplayName("GET /app/admin/csv debe retornar vista con lista de personas")
    void testAdminCsvPage() throws Exception {
      when(donacionesGateway.obtenerPersonas())
          .thenReturn(List.of(Map.of("id", UUID.randomUUID(), "nombre", "Comedor Sur")));

      mockMvc
          .perform(get("/app/admin/csv").principal(adminAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/admin/csv"))
          .andExpect(model().attributeExists("personas"));
    }

    @Test
    @DisplayName("POST /app/admin/csv con archivo multipart exitoso procesa y setea flashOk")
    void testImportarCsvExitoso() throws Exception {
      MockMultipartFile csvFile =
          new MockMultipartFile(
              "file",
              "personas.csv",
              "text/csv",
              "nombre,apellido,email\nJuan,Perez,juan@mail.com".getBytes());

      when(donacionesGateway.importarCsv(any())).thenReturn("1 registros procesados");

      mockMvc
          .perform(multipart("/app/admin/csv").file(csvFile).principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/csv"))
          .andExpect(
              flash().attribute("flashOk", "Importación completada: 1 registros procesados"));

      verify(donacionesGateway, times(1)).importarCsv(any());
    }

    @Test
    @DisplayName("POST /app/admin/csv con archivo corrupto o error setea flashError")
    void testImportarCsvError() throws Exception {
      MockMultipartFile csvFile =
          new MockMultipartFile(
              "file", "personas_corrupto.csv", "text/csv", "bad;format".getBytes());

      when(donacionesGateway.importarCsv(any()))
          .thenThrow(new RuntimeException("Formato de columnas inválido"));

      mockMvc
          .perform(multipart("/app/admin/csv").file(csvFile).principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/csv"))
          .andExpect(
              flash().attribute("flashError", containsString("Formato de columnas inválido")));
    }
  }

  // ==========================================
  // ADMINISTRACIÓN DE USUARIOS, ROLES Y ONGS
  // ==========================================

  @Nested
  @DisplayName("Administración de Usuarios y Solicitudes de ONGs")
  class UsuariosYRolesTests {

    @Test
    @DisplayName("GET /app/admin/usuarios debe cargar usuarios y solicitudes de adhesión")
    void testAdminUsuariosPage() throws Exception {
      when(authGateway.listarUsuarios())
          .thenReturn(
              List.of(
                  Map.of(
                      "id", UUID.randomUUID(),
                      "username", "voluntario1",
                      "rol", "DONANTE")));

      when(solicitudOngService.listarTodas())
          .thenReturn(
              List.of(
                  new SolicitudOngService.SolicitudOng(
                      "SOL-101",
                      "Hogar Esperanza",
                      "30-11223344-5",
                      "Calle Falsa 123",
                      "contacto@esperanza.org",
                      "+5411",
                      "Maria",
                      "Hogar",
                      "Frazadas",
                      "PENDIENTE",
                      "2026-11-10 12:00")));

      mockMvc
          .perform(get("/app/admin/usuarios").principal(adminAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/admin/usuarios"))
          .andExpect(model().attributeExists("usuarios"))
          .andExpect(model().attributeExists("solicitudes"));
    }

    @Test
    @DisplayName("POST /app/admin/usuarios/{id}/rol actualiza rol en AuthGateway exitosamente")
    void testCambiarRolExitoso() throws Exception {
      UUID usuarioId = UUID.randomUUID();

      mockMvc
          .perform(
              post("/app/admin/usuarios/" + usuarioId + "/rol")
                  .principal(adminAuth)
                  .param("rol", "BENEFICIARIA"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/usuarios"))
          .andExpect(flash().attribute("flashOk", "Rol actualizado exitosamente a BENEFICIARIA"));

      verify(authGateway, times(1)).cambiarRol(usuarioId, "BENEFICIARIA");
    }

    @Test
    @DisplayName("POST /app/admin/usuarios/{id}/rol con error en gateway setea flashError")
    void testCambiarRolError() throws Exception {
      UUID usuarioId = UUID.randomUUID();
      doThrow(new RuntimeException("Usuario no encontrado"))
          .when(authGateway)
          .cambiarRol(usuarioId, "ADMIN");

      mockMvc
          .perform(
              post("/app/admin/usuarios/" + usuarioId + "/rol")
                  .principal(adminAuth)
                  .param("rol", "ADMIN"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/usuarios"))
          .andExpect(flash().attribute("flashError", containsString("Usuario no encontrado")));
    }

    @Test
    @DisplayName("POST /app/admin/usuarios/{id}/reset-password restablece contraseña a password123")
    void testResetPasswordExitoso() throws Exception {
      UUID usuarioId = UUID.randomUUID();

      mockMvc
          .perform(
              post("/app/admin/usuarios/" + usuarioId + "/reset-password")
                  .principal(adminAuth)
                  .param("email", "usuario@mail.com"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/usuarios"))
          .andExpect(
              flash()
                  .attribute(
                      "flashOk",
                      "Contraseña del usuario usuario@mail.com restablecida con éxito a 'password123'."));

      verify(authGateway, times(1)).resetearPassword(usuarioId);
    }

    @Test
    @DisplayName(
        "POST /app/admin/solicitudes-ong/{id}/aprobar aprueba ONG y actualiza rol del usuario a BENEFICIARIA")
    void testAprobarSolicitudOngExitoso() throws Exception {
      // Arrange
      String solId = "SOL-202";
      UUID userId = UUID.randomUUID();

      SolicitudOngService.SolicitudOng solicitud =
          new SolicitudOngService.SolicitudOng(
              solId,
              "Comedor Los Niños",
              "30-55667788-9",
              "Av. Corrientes 1000",
              "contacto@losninos.org",
              "+5411",
              "Laura",
              "Comedor",
              "Alimentos",
              "PENDIENTE",
              "2026-11-10 14:00");

      when(solicitudOngService.obtenerPorId(solId)).thenReturn(Optional.of(solicitud));
      when(authGateway.listarUsuarios())
          .thenReturn(
              List.of(
                  Map.of(
                      "id", userId.toString(),
                      "email", "contacto@losninos.org",
                      "rol", "DONANTE")));

      // Act & Assert
      mockMvc
          .perform(post("/app/admin/solicitudes-ong/" + solId + "/aprobar").principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/usuarios"))
          .andExpect(
              flash()
                  .attribute(
                      "flashOk",
                      "Solicitud de ONG #" + solId + " verificada y aprobada con éxito."));

      verify(solicitudOngService, times(1)).actualizarEstado(solId, "APROBADA");
      verify(authGateway, times(1)).cambiarRol(userId, "BENEFICIARIA");
    }

    @Test
    @DisplayName(
        "POST /app/admin/solicitudes-ong/{id}/aprobar aprueba solicitud aunque aún no exista usuario con ese email")
    void testAprobarSolicitudOngSinUsuarioCoincidente() throws Exception {
      // Arrange
      String solId = "SOL-999";
      SolicitudOngService.SolicitudOng solicitud =
          new SolicitudOngService.SolicitudOng(
              solId,
              "Hogar Sin Cuenta",
              "30-99999999-9",
              "Calle 1",
              "noexiste@hogar.org",
              "+5411",
              "Director",
              "Hogar",
              "Abrigo",
              "PENDIENTE",
              "2026-11-10 16:00");

      when(solicitudOngService.obtenerPorId(solId)).thenReturn(Optional.of(solicitud));
      when(authGateway.listarUsuarios()).thenReturn(Collections.emptyList());

      // Act & Assert
      mockMvc
          .perform(post("/app/admin/solicitudes-ong/" + solId + "/aprobar").principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/usuarios"))
          .andExpect(
              flash().attribute("flashOk", containsString("verificada y aprobada con éxito.")));

      verify(solicitudOngService, times(1)).actualizarEstado(solId, "APROBADA");
      verify(authGateway, never()).cambiarRol(any(), any());
    }
  }

  // ==========================================
  // OPERATORIA DE DEPÓSITO Y VENCIMIENTOS
  // ==========================================

  @Nested
  @DisplayName("Operatoria de Depósito: Recepción, Vencimiento y Auditoría")
  class OperatoriaDepositoTests {

    @Test
    @DisplayName(
        "GET /app/admin/donaciones/recepcion debe retornar vista de recepción con listado de personas")
    void testRecepcionDonacionForm() throws Exception {
      when(donacionesGateway.obtenerPersonas())
          .thenReturn(
              List.of(
                  Map.of("id", UUID.randomUUID().toString(), "nombre", "Donante A"),
                  Map.of("id", UUID.randomUUID().toString(), "nombre", "Donante B")));

      mockMvc
          .perform(get("/app/admin/donaciones/recepcion").principal(adminAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/admin/recepcion-donacion"))
          .andExpect(model().attributeExists("personas"))
          .andExpect(model().attribute("personas", hasSize(2)));

      verify(donacionesGateway, times(1)).obtenerPersonas();
    }

    @Test
    @DisplayName(
        "POST /app/admin/donaciones/recepcion exitoso registra donación en depósito y redirige a matchmaking")
    void testRegistrarRecepcionDonacionExitoso() throws Exception {
      UUID donanteId = UUID.randomUUID();

      mockMvc
          .perform(
              post("/app/admin/donaciones/recepcion")
                  .principal(adminAuth)
                  .param("idDonante", donanteId.toString())
                  .param("descripcion", "Caja de leche en polvo")
                  .param("cantidad", "15.0")
                  .param("unidadMedicion", "UNIDADES")
                  .param("nombreSubcategoria", "Lácteos")
                  .param("esUsado", "false")
                  .param("fechaVencimiento", "2027-01-01"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/matchmaking"))
          .andExpect(
              flash()
                  .attribute("flashOk", "Donación recibida e ingresada a depósito correctamente."));

      verify(donacionesGateway, times(1))
          .crearRecepcion(
              argThat(
                  payload ->
                      donanteId.toString().equals(payload.get("idDonante"))
                          && payload.containsKey("bienesBrutos")
                          && payload.get("bienesBrutos") instanceof List<?> list
                          && !list.isEmpty()));
    }

    @Test
    @DisplayName(
        "POST /app/admin/donaciones/recepcion con campos inválidos retorna formulario con error")
    void testRegistrarRecepcionDonacionInvalido() throws Exception {
      when(donacionesGateway.obtenerPersonas()).thenReturn(Collections.emptyList());

      mockMvc
          .perform(
              post("/app/admin/donaciones/recepcion")
                  .principal(adminAuth)
                  .param("idDonante", "")
                  .param("descripcion", "")
                  .param("cantidad", "-5.0")
                  .param("unidadMedicion", "")
                  .param("nombreSubcategoria", ""))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/admin/recepcion-donacion"))
          .andExpect(model().attributeExists("errorForm"))
          .andExpect(model().attributeExists("personas"));

      verify(donacionesGateway, never()).crearRecepcion(any());
    }

    @Test
    @DisplayName(
        "POST /app/admin/donaciones/recepcion con error en gateway redirige a formulario con flashError")
    void testRegistrarRecepcionDonacionErrorGateway() throws Exception {
      UUID donanteId = UUID.randomUUID();
      doThrow(new RuntimeException("Error en base de datos"))
          .when(donacionesGateway)
          .crearRecepcion(any());

      mockMvc
          .perform(
              post("/app/admin/donaciones/recepcion")
                  .principal(adminAuth)
                  .param("idDonante", donanteId.toString())
                  .param("descripcion", "Fideos secos")
                  .param("cantidad", "10.0")
                  .param("unidadMedicion", "KILOGRAMOS")
                  .param("nombreSubcategoria", "Alimentos")
                  .param("esUsado", "false"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/donaciones/recepcion"))
          .andExpect(
              flash()
                  .attribute(
                      "flashError",
                      containsString(
                          "Error al registrar la donación en depósito: Error en base de datos")));
    }

    @Test
    @DisplayName(
        "POST /app/admin/donaciones/{id}/vencer marca donación como vencida con código de seguimiento")
    void testVencerDonacionExitoso() throws Exception {
      UUID donacionId = UUID.fromString("4444a111-0000-4000-8000-000000000001");

      mockMvc
          .perform(post("/app/admin/donaciones/{id}/vencer", donacionId).principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/matchmaking"))
          .andExpect(
              flash()
                  .attribute(
                      "flashOk",
                      containsString("La donación #DT-4444 ha sido marcada como VENCIDA.")));

      verify(donacionesGateway, times(1)).marcarDonacionVencida(donacionId);
    }

    @Test
    @DisplayName("POST /app/admin/donaciones/{id}/vencer con error en gateway setea flashError")
    void testVencerDonacionError() throws Exception {
      UUID donacionId = UUID.randomUUID();
      doThrow(new RuntimeException("Donación no encontrada"))
          .when(donacionesGateway)
          .marcarDonacionVencida(donacionId);

      mockMvc
          .perform(post("/app/admin/donaciones/{id}/vencer", donacionId).principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/matchmaking"))
          .andExpect(
              flash()
                  .attribute(
                      "flashError",
                      containsString(
                          "No se pudo marcar la donación como vencida: Donación no encontrada")));
    }

    @Test
    @DisplayName(
        "POST /app/admin/donaciones/auditoria-vencidos ejecuta barrido exitoso y setea flashOk")
    void testAuditarDonacionesVencidasExitoso() throws Exception {
      mockMvc
          .perform(post("/app/admin/donaciones/auditoria-vencidos").principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/matchmaking"))
          .andExpect(
              flash()
                  .attribute(
                      "flashOk",
                      "Auditoría de vencimientos ejecutada exitosamente sobre el depósito."));

      verify(donacionesGateway, times(1)).auditarVencidos();
    }

    @Test
    @DisplayName(
        "POST /app/admin/donaciones/auditoria-vencidos con error en gateway setea flashError")
    void testAuditarDonacionesVencidasError() throws Exception {
      doThrow(new RuntimeException("Fallo en servicio de inventario"))
          .when(donacionesGateway)
          .auditarVencidos();

      mockMvc
          .perform(post("/app/admin/donaciones/auditoria-vencidos").principal(adminAuth))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/admin/matchmaking"))
          .andExpect(
              flash()
                  .attribute(
                      "flashError",
                      containsString(
                          "Error al ejecutar la auditoría de vencimientos: Fallo en servicio de inventario")));
    }
  }
}
