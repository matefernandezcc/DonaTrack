package com.donatrack.web;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.donatrack.web.controller.AdminController;
import com.donatrack.web.gateway.AuthGateway;
import com.donatrack.web.gateway.DonacionesGateway;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@DisplayName("DonanteFlowTest - Flujos del Portal de Donantes")
class DonanteFlowTest {

  private MockMvc mockMvc;

  private DonacionesGateway donacionesGateway;
  private IncentivosGateway incentivosGateway;
  private LogisticaGateway logisticaGateway;
  private AuthGateway authGateway;
  private SolicitudOngService solicitudOngService;

  private JwtAuthenticationToken donanteAuth;
  private UUID donantePersonaId;

  @BeforeEach
  void setUp() {
    donacionesGateway = mock(DonacionesGateway.class);
    incentivosGateway = mock(IncentivosGateway.class);
    logisticaGateway = mock(LogisticaGateway.class);
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

    donantePersonaId = UUID.fromString("a1111111-1111-4111-8111-111111111111");

    Jwt jwt =
        Jwt.withTokenValue("mock-jwt-donante")
            .header("alg", "none")
            .subject(UUID.randomUUID().toString())
            .claim("name", "María González")
            .claim("username", "mgonzalez")
            .claim("roles", List.of("DONANTE"))
            .claim("personaId", donantePersonaId.toString())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

    donanteAuth =
        new JwtAuthenticationToken(
            jwt, List.of(new SimpleGrantedAuthority("ROLE_DONANTE")), "María González");
  }

  // ==========================================
  // PANEL Y PESTAÑAS DEL DONANTE
  // ==========================================

  @Nested
  @DisplayName("Panel General y Navegación por Tabs")
  class PanelDonanteTests {

    @Test
    @DisplayName("GET /app/donante por defecto debe abrir tab 'resumen' y cargar métricas base")
    void testPanelResumenDefault() throws Exception {
      // Arrange
      when(incentivosGateway.obtenerMetricas(donantePersonaId))
          .thenReturn(
              Map.of(
                  "totalDonacionesHistoricas", 5L,
                  "cantidadBienesDonados", 25L,
                  "donacionesExitosas", 3L,
                  "puntosTotales", 185L));

      when(incentivosGateway.obtenerInsignias(donantePersonaId))
          .thenReturn(
              List.of(
                  Map.of("id", "INS-1", "nombre", "Primer Impacto"),
                  Map.of("id", "INS-2", "nombre", "Generosidad Constante")));

      when(donacionesGateway.listarDonaciones(null, donantePersonaId))
          .thenReturn(
              List.of(
                  Map.of(
                      "id", UUID.randomUUID(),
                      "estado", "ENTREGADA",
                      "correoBeneficiario", "comedor@esperanza.org")));

      when(donacionesGateway.obtenerBeneficiarios()).thenReturn(Collections.emptyList());

      // Act & Assert
      mockMvc
          .perform(get("/app/donante").principal(donanteAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/donante/panel"))
          .andExpect(model().attribute("tabActiva", "resumen"))
          .andExpect(model().attribute("totalInsignias", 2))
          .andExpect(model().attribute("puntosReales", 185L))
          .andExpect(model().attribute("rangoNombre", "Guardián Solidario"))
          .andExpect(model().attribute("rangoNivel", 3))
          .andExpect(model().attribute("puntosFaltantes", 165L)) // 350 - 185 = 165
          .andExpect(model().attribute("isNuevoUsuario", false))
          .andExpect(model().attributeExists("kilosTotales"))
          .andExpect(model().attributeExists("platosComida"))
          .andExpect(model().attributeExists("comedoresAsistidos"))
          .andExpect(model().attributeExists("misDonaciones"))
          .andExpect(model().attributeExists("causasActivas"));
    }

    @ParameterizedTest(name = "Tab {0} debe setear tabActiva={0}")
    @CsvSource({"mis-donaciones", "causas", "insignias", "resumen"})
    void testTabsNavegacion(String tab) throws Exception {
      when(incentivosGateway.obtenerMetricas(donantePersonaId)).thenReturn(Collections.emptyMap());
      when(donacionesGateway.listarDonaciones(null, donantePersonaId))
          .thenReturn(Collections.emptyList());
      when(donacionesGateway.obtenerBeneficiarios()).thenReturn(Collections.emptyList());

      mockMvc
          .perform(get("/app/donante").principal(donanteAuth).param("tab", tab))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/donante/panel"))
          .andExpect(model().attribute("tabActiva", tab));
    }

    @ParameterizedTest(name = "Puntos {0} deben clasificar como {1} (Nivel {2})")
    @CsvSource({
      "25, Donante Inicial, 1",
      "50, Colaborador Activo, 2",
      "149, Colaborador Activo, 2",
      "150, Guardián Solidario, 3",
      "349, Guardián Solidario, 3",
      "350, Líder Comunitario, 4",
      "1200, Líder Comunitario, 4"
    })
    void testCalculoRangosDinamicos(long puntos, String rangoEsperado, int nivelEsperado)
        throws Exception {
      when(incentivosGateway.obtenerMetricas(donantePersonaId))
          .thenReturn(
              Map.of(
                  "totalDonacionesHistoricas", 2L,
                  "cantidadBienesDonados", 10L,
                  "puntosTotales", puntos));

      when(donacionesGateway.listarDonaciones(null, donantePersonaId))
          .thenReturn(Collections.emptyList());
      when(donacionesGateway.obtenerBeneficiarios()).thenReturn(Collections.emptyList());

      mockMvc
          .perform(get("/app/donante").principal(donanteAuth))
          .andExpect(status().isOk())
          .andExpect(model().attribute("rangoNombre", rangoEsperado))
          .andExpect(model().attribute("rangoNivel", nivelEsperado));
    }

    @Test
    @DisplayName(
        "Etapas de trazabilidad deben mapearse adecuadamente según el estado de la donación")
    void testMapeoEtapasTrazabilidad() throws Exception {
      // Arrange
      UUID d1 = UUID.randomUUID();
      UUID d2 = UUID.randomUUID();
      UUID d3 = UUID.randomUUID();

      when(donacionesGateway.listarDonaciones(null, donantePersonaId))
          .thenReturn(
              List.of(
                  Map.of("id", d1, "estado", "EN_DEPOSITO"),
                  Map.of("id", d2, "estado", "ASIGNADA", "correoBeneficiario", "comedor@sur.org"),
                  Map.of(
                      "id", d3, "estado", "ENTREGADA", "correoBeneficiario", "hogar@norte.org")));

      when(incentivosGateway.obtenerMetricas(donantePersonaId)).thenReturn(Collections.emptyMap());
      when(donacionesGateway.obtenerBeneficiarios()).thenReturn(Collections.emptyList());

      // Act & Assert
      mockMvc
          .perform(get("/app/donante").principal(donanteAuth))
          .andExpect(status().isOk())
          .andExpect(model().attribute("misDonaciones", hasSize(3)))
          .andExpect(
              model()
                  .attribute(
                      "misDonaciones",
                      hasItem(
                          allOf(
                              hasEntry("id", (Object) d1),
                              hasEntry("etapaNumero", (Object) 1),
                              hasEntry(
                                  "estadoLegible", (Object) "En Depósito · Auditada (+10 pts)")))))
          .andExpect(
              model()
                  .attribute(
                      "misDonaciones",
                      hasItem(
                          allOf(
                              hasEntry("id", (Object) d2),
                              hasEntry("etapaNumero", (Object) 2),
                              hasEntry("estadoLegible", (Object) "Asignada a Comedor · En Ruta")))))
          .andExpect(
              model()
                  .attribute(
                      "misDonaciones",
                      hasItem(
                          allOf(
                              hasEntry("id", (Object) d3),
                              hasEntry("etapaNumero", (Object) 3),
                              hasEntry(
                                  "estadoLegible", (Object) "Entregada en Destino (+25 pts)")))));
    }

    @Test
    @DisplayName(
        "Donante sin donaciones personales debe utilizar fallback histórico de DEFAULT_DONANTE_ID")
    void testPanelDonanteFallbackDefaultDonante() throws Exception {
      // Arrange: donantePersonaId custom no tiene donaciones, pero DEFAULT_DONANTE_ID sí
      UUID customDonante = UUID.randomUUID();
      Jwt jwtCustom =
          Jwt.withTokenValue("mock-custom-donante")
              .header("alg", "none")
              .subject(UUID.randomUUID().toString())
              .claim("name", "Nuevo Usuario")
              .claim("username", "nuevouser")
              .claim("roles", List.of("DONANTE"))
              .claim("personaId", customDonante.toString())
              .build();
      JwtAuthenticationToken customAuth =
          new JwtAuthenticationToken(
              jwtCustom, List.of(new SimpleGrantedAuthority("ROLE_DONANTE")), "Nuevo Usuario");

      UUID defaultDonanteId = UUID.fromString("a1111111-1111-4111-8111-111111111111");

      when(donacionesGateway.listarDonaciones(null, customDonante))
          .thenReturn(Collections.emptyList());
      when(donacionesGateway.listarDonaciones(null, defaultDonanteId))
          .thenReturn(
              List.of(
                  Map.of(
                      "id", UUID.randomUUID(),
                      "estado", "EN_DEPOSITO",
                      "descripcion", "Donación comunitaria de respaldo")));

      when(incentivosGateway.obtenerMetricas(customDonante)).thenReturn(Collections.emptyMap());
      when(donacionesGateway.obtenerBeneficiarios()).thenReturn(Collections.emptyList());

      // Act & Assert
      mockMvc
          .perform(get("/app/donante").principal(customAuth))
          .andExpect(status().isOk())
          .andExpect(model().attribute("misDonaciones", hasSize(1)))
          .andExpect(model().attribute("isNuevoUsuario", false));

      verify(donacionesGateway, times(1)).listarDonaciones(null, customDonante);
      verify(donacionesGateway, times(1)).listarDonaciones(null, defaultDonanteId);
    }

    @Test
    @DisplayName(
        "donantePanel es resiliente y degrada gracefully ante fallos de microservicios secundarios")
    void donantePanel_resilienteAnteFalloDeMicroservicios() throws Exception {
      // Arrange: simular fallo/cold-start en microservicios secundarios
      when(incentivosGateway.obtenerMetricas(any()))
          .thenThrow(new RuntimeException("Simulated Render Cold-Start"));
      when(incentivosGateway.obtenerInsignias(any()))
          .thenThrow(new RuntimeException("Simulated Insignias Timeout"));
      when(incentivosGateway.obtenerMisiones(any()))
          .thenThrow(new RuntimeException("Simulated Misiones Error"));
      when(donacionesGateway.listarDonaciones(any(), any()))
          .thenThrow(new RuntimeException("Simulated Donaciones Timeout"));
      when(donacionesGateway.obtenerBeneficiarios())
          .thenThrow(new RuntimeException("Simulated Beneficiarios Error"));

      // Act & Assert
      mockMvc
          .perform(get("/app/donante").principal(donanteAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/donante/panel"))
          .andExpect(model().attributeExists("tabActiva"))
          .andExpect(model().attributeExists("misDonaciones"))
          .andExpect(model().attributeExists("beneficiarios"))
          .andExpect(model().attributeExists("causasActivas"))
          .andExpect(model().attributeExists("metricas"))
          .andExpect(model().attributeExists("insignias"))
          .andExpect(model().attribute("tabActiva", "resumen"))
          .andExpect(model().attribute("totalInsignias", 0))
          .andExpect(model().attribute("isNuevoUsuario", true))
          .andExpect(model().attribute("rangoNombre", "Donante Inicial"))
          .andExpect(model().attribute("rangoNivel", 1));
    }
  }

  // ==========================================
  // FORMULARIO Y ALTA DE DONACIONES
  // ==========================================

  @Nested
  @DisplayName("Formulario y Alta de Donaciones")
  class AltaDonacionesTests {

    @Test
    @DisplayName("GET /app/donante/donaciones sin prefill carga vista limpia")
    void testFormularioDonacionSinPrefill() throws Exception {
      mockMvc
          .perform(get("/app/donante/donaciones").principal(donanteAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/donante/donaciones"))
          .andExpect(model().attribute("nombreUsuario", "María González"))
          .andExpect(model().attributeDoesNotExist("prefillDescripcion"))
          .andExpect(model().attributeDoesNotExist("prefillCategoria"))
          .andExpect(model().attributeDoesNotExist("prefillOng"));
    }

    @Test
    @DisplayName("GET /app/donante/donaciones con parámetros prefill debe cargarlos en el modelo")
    void testFormularioDonacionConPrefill() throws Exception {
      mockMvc
          .perform(
              get("/app/donante/donaciones")
                  .principal(donanteAuth)
                  .param("descripcion", "Fideos y Arroz")
                  .param("categoria", "Alimentos")
                  .param("ong", "Comedor El Faro"))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/donante/donaciones"))
          .andExpect(model().attribute("prefillDescripcion", "Fideos y Arroz"))
          .andExpect(model().attribute("prefillCategoria", "Alimentos"))
          .andExpect(model().attribute("prefillOng", "Comedor El Faro"));
    }

    @Test
    @DisplayName(
        "POST /app/donante/donaciones exitoso debe registrar recepción, sumar puntos en incentivos y redirigir")
    void testCrearDonacionExitoso() throws Exception {
      // Arrange
      UUID donacionGenId = UUID.randomUUID();
      Map<String, Object> respRecepcion =
          Map.of(
              "id", donacionGenId.toString(),
              "donacionesSegmentadas", List.of(Map.of("id", donacionGenId.toString())));

      when(donacionesGateway.crearRecepcion(any())).thenReturn(respRecepcion);

      // Act & Assert
      mockMvc
          .perform(
              post("/app/donante/donaciones")
                  .principal(donanteAuth)
                  .param("descripcion", "Caja con 20 paquetes de leche en polvo")
                  .param("cantidad", "20.0")
                  .param("unidadMedicion", "UNIDADES")
                  .param("nombreSubcategoria", "Lácteos")
                  .param("esUsado", "false"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/donante?tab=seguimiento"))
          .andExpect(flash().attributeExists("flashOk"))
          .andExpect(flash().attribute("flashOk", "Donación registrada con éxito. ¡Gracias por tu aporte solidario!"));

      verify(donacionesGateway, times(1)).crearRecepcion(any());
      verify(incentivosGateway, times(1)).registrarActividad(eq(donantePersonaId), any());
    }

    @Test
    @DisplayName(
        "POST /app/donante/donaciones con error 502/Bad Gateway debe mostrar mensaje amigable")
    void testCrearDonacionError502() throws Exception {
      // Arrange
      when(donacionesGateway.crearRecepcion(any()))
          .thenThrow(new RuntimeException("502 Bad Gateway: Connection refused"));

      // Act & Assert
      mockMvc
          .perform(
              post("/app/donante/donaciones")
                  .principal(donanteAuth)
                  .param("descripcion", "Ropa")
                  .param("cantidad", "10")
                  .param("unidadMedicion", "UNIDADES")
                  .param("nombreSubcategoria", "Abrigo"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/donante"))
          .andExpect(
              flash()
                  .attribute(
                      "flashError",
                      "No se pudo registrar la donación. Por favor, intentá nuevamente más tarde."));
    }

    @Test
    @DisplayName("POST /app/donante/donaciones con timeout debe mostrar mensaje amigable de espera")
    void testCrearDonacionErrorTimeout() throws Exception {
      // Arrange
      when(donacionesGateway.crearRecepcion(any()))
          .thenThrow(new RuntimeException("Read timed out"));

      // Act & Assert
      mockMvc
          .perform(
              post("/app/donante/donaciones")
                  .principal(donanteAuth)
                  .param("descripcion", "Útiles")
                  .param("cantidad", "5")
                  .param("unidadMedicion", "UNIDADES")
                  .param("nombreSubcategoria", "Escolar"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/donante"))
          .andExpect(
              flash()
                  .attribute(
                      "flashError",
                      "No se pudo registrar la donación. Por favor, intentá nuevamente más tarde."));
    }

    @Test
    @DisplayName(
        "POST /app/donante/donaciones sin array de segmentadas usa id de la raíz para flashOk")
    void testCrearDonacionSinSegmentadasFallbackId() throws Exception {
      // Arrange
      when(donacionesGateway.crearRecepcion(any())).thenReturn(Map.of("id", "REC-8888-PLANO"));

      // Act & Assert
      mockMvc
          .perform(
              post("/app/donante/donaciones")
                  .principal(donanteAuth)
                  .param("descripcion", "Caja de libros escolares")
                  .param("cantidad", "15")
                  .param("unidadMedicion", "KILOGRAMOS")
                  .param("nombreSubcategoria", "Educación"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/donante?tab=seguimiento"))
          .andExpect(flash().attribute("flashOk", "Donación registrada con éxito. ¡Gracias por tu aporte solidario!"));
    }

    @Test
    @DisplayName(
        "GET /app/donante?tab=seguimiento normaliza subcategoriaGrupo y misiones enriquecidas")
    void testPanelNormalizaSubcategoriaYMisiones() throws Exception {
      when(incentivosGateway.obtenerMetricas(donantePersonaId)).thenReturn(Collections.emptyMap());
      when(incentivosGateway.obtenerInsignias(donantePersonaId))
          .thenReturn(Collections.emptyList());
      when(incentivosGateway.obtenerMisiones(donantePersonaId))
          .thenReturn(
              List.of(
                  Map.of(
                      "nombre", "Primer Impacto",
                      "tipoMetrica", "DONACIONES_EXITOSAS",
                      "objetivo", 1,
                      "valorActual", 1,
                      "porcentaje", 100,
                      "faltante", 0,
                      "completada", true)));
      when(donacionesGateway.listarDonaciones(null, donantePersonaId))
          .thenReturn(
              List.of(
                  Map.of(
                      "id",
                      UUID.randomUUID().toString(),
                      "descripcion",
                      "Frazadas de invierno",
                      "cantidad",
                      4,
                      "unidad",
                      "UNIDADES",
                      "subcategoria",
                      Map.of("nombre", "Ropa de Abrigo"),
                      "estado",
                      "EN_DEPOSITO")));

      mockMvc
          .perform(get("/app/donante").param("tab", "seguimiento").principal(donanteAuth))
          .andExpect(status().isOk())
          .andExpect(
              model().attribute("misDonaciones", hasItem(hasEntry("subcategoriaGrupo", "ABRIGO"))))
          .andExpect(model().attribute("misiones", hasItem(hasEntry("unidadLabel", "aportes"))))
          .andExpect(model().attribute("misiones", hasItem(hasEntry("porcentaje", 100))))
          .andExpect(model().attribute("misiones", hasItem(hasEntry("completada", true))));
    }
  }
}
