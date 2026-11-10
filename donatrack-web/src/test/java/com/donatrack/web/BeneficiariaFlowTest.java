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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@DisplayName("BeneficiariaFlowTest - Flujos del Portal de Organizaciones Beneficiarias y Comedores")
class BeneficiariaFlowTest {

  private MockMvc mockMvc;

  private DonacionesGateway donacionesGateway;
  private IncentivosGateway incentivosGateway;
  private LogisticaGateway logisticaGateway;
  private AuthGateway authGateway;
  private SolicitudOngService solicitudOngService;

  private JwtAuthenticationToken benefAuth;
  private JwtAuthenticationToken adminAuth;
  private UUID beneficiarioPersonaId;

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

    beneficiarioPersonaId = UUID.fromString("b2222222-2222-4222-8222-222222222222");

    Jwt jwtBenef =
        Jwt.withTokenValue("mock-jwt-benef")
            .header("alg", "none")
            .subject(UUID.randomUUID().toString())
            .claim("name", "Comedor Esperanza")
            .claim("username", "esperanza")
            .claim("roles", List.of("BENEFICIARIA"))
            .claim("personaId", beneficiarioPersonaId.toString())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

    benefAuth =
        new JwtAuthenticationToken(
            jwtBenef,
            List.of(new SimpleGrantedAuthority("ROLE_BENEFICIARIA")),
            "Comedor Esperanza");

    Jwt jwtAdmin =
        Jwt.withTokenValue("mock-jwt-admin")
            .header("alg", "none")
            .subject(UUID.randomUUID().toString())
            .claim("name", "Admin")
            .claim("username", "admin")
            .claim("roles", List.of("ADMIN"))
            .build();

    adminAuth =
        new JwtAuthenticationToken(
            jwtAdmin, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), "admin");
  }

  // ==========================================
  // PANEL PRINCIPAL BENEFICIARIA
  // ==========================================

  @Nested
  @DisplayName("Panel de Control Comunitario")
  class PanelBeneficiariaTests {

    @Test
    @DisplayName(
        "GET /app/beneficiaria debe cargar necesidades sanitizadas, entregas en camino y kilos recibidos")
    void testPanelBeneficiariaExitoso() throws Exception {
      // Arrange
      when(donacionesGateway.obtenerNecesidades(beneficiarioPersonaId))
          .thenReturn(
              List.of(
                  Map.of(
                      "id",
                      UUID.randomUUID(),
                      "descripcion",
                      "Leche y cacao para meriendas",
                      "cantidadRequerida",
                      50.0,
                      "subcategoriaRequerida",
                      Map.of("nombre", "Lácteos"),
                      "tipo",
                      "Periódica"),
                  Map.of(
                      "id", UUID.randomUUID(),
                      "descripcion", "Frazadas polares",
                      "cantidadObjetivo", 20.0,
                      "subcategoriaRequerida", "Abrigo",
                      "tipoPeriodo", "Extraordinaria")));

      when(donacionesGateway.obtenerBeneficiario(beneficiarioPersonaId))
          .thenReturn(
              Map.of(
                  "correoRepresentante",
                  "coordinacion@esperanza.org",
                  "persona",
                  Map.of(
                      "razonSocial", "Asociación Civil Comedor La Esperanza",
                      "docNumero", "30-71482910-4",
                      "direccion",
                          Map.of(
                              "calle", "Av. San Martín",
                              "altura", "1450",
                              "localidad", "La Plata"))));

      Map<String, Object> parada1 =
          Map.of(
              "direccion", Map.of("calle", "San Martín", "altura", "1450", "localidad", "La Plata"),
              "coordenada", Map.of("latitud", -34.92, "longitud", -57.95),
              "entregas",
                  List.of(
                      Map.of(
                          "idEntrega",
                          "3300-AA",
                          "estado",
                          "ENTREGADA",
                          "pesoEstimado",
                          150.0,
                          "volumenEstimado",
                          1.5,
                          "comprobanteRecepcion",
                          Map.of(
                              "fotos", List.of("https://storage.donatrack.org/remito-3300.jpg"))),
                      Map.of(
                          "idEntrega",
                          "3400-BB",
                          "estado",
                          "EN_CAMINO",
                          "pesoEstimado",
                          80.0,
                          "volumenEstimado",
                          0.8)));

      Map<String, Object> ruta1 =
          Map.of(
              "fechaOperativa", "2026-11-10",
              "iniciada", true,
              "camion", Map.of("patente", "AF123CD"),
              "chofer", Map.of("nombre", "Juan Chofer", "legajo", "CH-202"),
              "paradas", List.of(parada1));

      when(logisticaGateway.listarRutas()).thenReturn(List.of(ruta1));

      // Act & Assert
      mockMvc
          .perform(get("/app/beneficiaria").principal(benefAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/beneficiaria/panel"))
          .andExpect(model().attribute("totalNecesidades", 2))
          .andExpect(model().attribute("totalEnCamino", 1))
          .andExpect(model().attribute("totalRecibidas", 1))
          .andExpect(model().attribute("kilosRecibidos", 150L))
          .andExpect(
              model().attribute("nombreOrganizacion", "Asociación Civil Comedor La Esperanza"))
          .andExpect(model().attribute("cuitOrganizacion", "30-71482910-4"))
          .andExpect(model().attribute("direccionOrganizacion", "Av. San Martín 1450, La Plata"))
          .andExpect(model().attribute("emailOrganizacion", "coordinacion@esperanza.org"))
          .andExpect(model().attributeExists("proximasEntregas"));

      verify(donacionesGateway, times(1)).obtenerNecesidades(beneficiarioPersonaId);
      verify(logisticaGateway, times(1)).listarRutas();
    }

    @Test
    @DisplayName(
        "GET /app/beneficiaria con vista ADMIN debe indicar (Vista Admin) y resolver correctamente")
    void testPanelBeneficiariaVistaAdmin() throws Exception {
      when(donacionesGateway.obtenerNecesidades(any())).thenReturn(Collections.emptyList());
      when(donacionesGateway.obtenerBeneficiario(any())).thenReturn(Collections.emptyMap());
      when(logisticaGateway.listarRutas()).thenReturn(Collections.emptyList());

      mockMvc
          .perform(get("/app/beneficiaria").principal(adminAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/beneficiaria/panel"))
          .andExpect(model().attribute("nombreOrganizacion", "Fundación Esperanza (Vista Admin)"));
    }
  }

  // ==========================================
  // DECLARACIÓN DE NECESIDADES
  // ==========================================

  @Nested
  @DisplayName("Gestión de Necesidades Comunitarias")
  class NecesidadesTests {

    @Test
    @DisplayName("GET /app/beneficiaria/necesidades debe listar necesidades de la organización")
    void testListarNecesidades() throws Exception {
      when(donacionesGateway.obtenerNecesidades(beneficiarioPersonaId))
          .thenReturn(
              List.of(
                  Map.of(
                      "id",
                      UUID.randomUUID(),
                      "descripcion",
                      "Arroz largo fino",
                      "cantidadRequerida",
                      100.0,
                      "subcategoriaRequerida",
                      Map.of("nombre", "Alimentos Secos"),
                      "tipo",
                      "Ordinaria")));

      mockMvc
          .perform(get("/app/beneficiaria/necesidades").principal(benefAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/beneficiaria/necesidades"))
          .andExpect(model().attributeExists("necesidades"))
          .andExpect(model().attribute("necesidades", hasSize(1)));
    }

    @Test
    @DisplayName(
        "POST /app/beneficiaria/necesidades exitoso debe registrar en gateway y setear flashOk")
    void testCrearNecesidadExitoso() throws Exception {
      mockMvc
          .perform(
              post("/app/beneficiaria/necesidades")
                  .principal(benefAuth)
                  .param("descripcion", "Zapatillas infantiles talles 28 a 34")
                  .param("cantidadRequerida", "30.0")
                  .param("tipo", "Extraordinaria")
                  .param("nombreSubcategoria", "Calzado"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/beneficiaria/necesidades"))
          .andExpect(flash().attribute("flashOk", "Necesidad declarada con éxito."));

      verify(donacionesGateway, times(1))
          .agregarNecesidad(
              eq(beneficiarioPersonaId),
              eq(
                  Map.of(
                      "tipo",
                      "Extraordinaria",
                      "descripcion",
                      "Zapatillas infantiles talles 28 a 34",
                      "cantidadRequerida",
                      30.0,
                      "subcategoriaRequerida",
                      Map.of(
                          "nombre", "Calzado",
                          "descripcion", "Categoría Calzado"))));
    }

    @Test
    @DisplayName("POST /app/beneficiaria/necesidades con error en gateway setea flashError")
    void testCrearNecesidadError() throws Exception {
      doThrow(new RuntimeException("Microservicio de donaciones no responde"))
          .when(donacionesGateway)
          .agregarNecesidad(any(), any());

      mockMvc
          .perform(
              post("/app/beneficiaria/necesidades")
                  .principal(benefAuth)
                  .param("descripcion", "Harina")
                  .param("cantidadRequerida", "10")
                  .param("tipo", "Ordinaria")
                  .param("nombreSubcategoria", "Alimentos"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/beneficiaria/necesidades"))
          .andExpect(
              flash()
                  .attribute(
                      "flashError",
                      containsString(
                          "Error al registrar necesidad: Microservicio de donaciones no responde")));
    }
  }

  // ==========================================
  // CONFIRMACIÓN Y RECEPCIÓN DE ENTREGAS
  // ==========================================

  @Nested
  @DisplayName("Recepción y Confirmación de Entregas")
  class EntregasTests {

    @Test
    @DisplayName("GET /app/beneficiaria/entregas debe retornar lista de rutas y entregas agrupadas")
    void testListarEntregas() throws Exception {
      when(logisticaGateway.listarRutas()).thenReturn(Collections.emptyList());

      mockMvc
          .perform(get("/app/beneficiaria/entregas").principal(benefAuth))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/beneficiaria/entregas"))
          .andExpect(model().attributeExists("rutas"))
          .andExpect(model().attributeExists("entregasEnCamino"))
          .andExpect(model().attributeExists("entregasRecibidas"))
          .andExpect(model().attribute("totalEnCamino", 0))
          .andExpect(model().attribute("totalRecibidas", 0));
    }

    @Test
    @DisplayName(
        "POST /app/beneficiaria/entregas/confirmar con fotoArchivo debe subir a S3 y confirmar")
    void testConfirmarEntregaConArchivo() throws Exception {
      // Arrange
      UUID donacionId = UUID.randomUUID();
      MockMultipartFile file =
          new MockMultipartFile(
              "fotoArchivo", "acta_entrega.jpg", "image/jpeg", "fake-image-bytes".getBytes());

      String s3Url =
          "https://nkqyubnzckxojsubyiuk.supabase.co/storage/v1/object/public/remitos/acta-123.jpg";
      when(logisticaGateway.subirFotoComprobante(eq(donacionId), any())).thenReturn(s3Url);

      // Act & Assert
      mockMvc
          .perform(
              multipart("/app/beneficiaria/entregas/confirmar")
                  .file(file)
                  .principal(benefAuth)
                  .param("idDonacion", donacionId.toString())
                  .param("patenteCamion", "AA111BB"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/beneficiaria/entregas"))
          .andExpect(
              flash()
                  .attribute(
                      "flashOk",
                      containsString("confirmada con éxito. Comprobante alojado en Supabase S3.")));

      verify(logisticaGateway, times(1)).subirFotoComprobante(eq(donacionId), any());
      verify(logisticaGateway, times(1)).confirmarEntrega(donacionId, List.of(s3Url), "AA111BB");
    }

    @Test
    @DisplayName("POST /app/beneficiaria/entregas/confirmar con fotoUrl debe usar dicha URL")
    void testConfirmarEntregaConUrl() throws Exception {
      UUID donacionId = UUID.randomUUID();
      String customUrl = "https://comedor.org/foto.jpg";

      mockMvc
          .perform(
              post("/app/beneficiaria/entregas/confirmar")
                  .principal(benefAuth)
                  .param("idDonacion", donacionId.toString())
                  .param("patenteCamion", "AF456CD")
                  .param("fotoUrl", customUrl))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/beneficiaria/entregas"))
          .andExpect(flash().attributeExists("flashOk"));

      verify(logisticaGateway, times(1))
          .confirmarEntrega(donacionId, List.of(customUrl), "AF456CD");
    }

    @Test
    @DisplayName(
        "POST /app/beneficiaria/entregas/confirmar sin foto usa comprobante default de Supabase")
    void testConfirmarEntregaDefaultSupabase() throws Exception {
      UUID donacionId = UUID.randomUUID();

      mockMvc
          .perform(
              post("/app/beneficiaria/entregas/confirmar")
                  .principal(benefAuth)
                  .param("idDonacion", donacionId.toString())
                  .param("patenteCamion", "AE999ZZ"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/beneficiaria/entregas"))
          .andExpect(flash().attributeExists("flashOk"));

      verify(logisticaGateway, times(1))
          .confirmarEntrega(
              eq(donacionId),
              eq(
                  List.of(
                      "https://nkqyubnzckxojsubyiuk.supabase.co/storage/v1/object/public/remitos/remito-verificado-app.jpeg")),
              eq("AE999ZZ"));
    }

    @Test
    @DisplayName("POST /app/beneficiaria/entregas/confirmar con error en gateway setea flashError")
    void testConfirmarEntregaError() throws Exception {
      UUID donacionId = UUID.randomUUID();
      doThrow(new RuntimeException("Camión no coincide con la hoja de ruta"))
          .when(logisticaGateway)
          .confirmarEntrega(any(), any(), any());

      mockMvc
          .perform(
              post("/app/beneficiaria/entregas/confirmar")
                  .principal(benefAuth)
                  .param("idDonacion", donacionId.toString())
                  .param("patenteCamion", "INCORRECTO"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/app/beneficiaria/entregas"))
          .andExpect(
              flash()
                  .attribute(
                      "flashError", containsString("Camión no coincide con la hoja de ruta")));
    }
  }
}
