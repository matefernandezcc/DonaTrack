package com.donatrack.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.donatrack.web.controller.AdminController;
import com.donatrack.web.dto.CamionDto;
import com.donatrack.web.gateway.DonacionesGateway;
import com.donatrack.web.gateway.IncentivosGateway;
import com.donatrack.web.gateway.LogisticaGateway;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

class AdminControllerTest {

  private MockMvc mockMvc;
  private List<CamionDto> camionesFake;

  @BeforeEach
  void setUp() {
    camionesFake = new ArrayList<>();
    camionesFake.add(new CamionDto("AA111BB", 20.0, 3.0, 4000.0));

    List<com.donatrack.web.dto.ChoferDto> choferesFake = new ArrayList<>();
    choferesFake.add(
        new com.donatrack.web.dto.ChoferDto("CH-001", "Mario", "Bros", "+54 11 1122-3344"));

    LogisticaGateway logisticaGateway =
        new LogisticaGateway(null) {
          @Override
          public List<CamionDto> listarCamiones() {
            return camionesFake;
          }

          @Override
          public void crearCamion(CamionDto camion) {
            camionesFake.add(camion);
          }

          @Override
          public void eliminarCamion(String patente) {
            camionesFake.removeIf(c -> c.patente().equals(patente));
          }

          @Override
          public List<com.donatrack.web.dto.ChoferDto> listarChoferes() {
            return choferesFake;
          }

          @Override
          public void crearChofer(com.donatrack.web.dto.ChoferDto chofer) {
            choferesFake.add(chofer);
          }

          @Override
          public List<java.util.Map<String, Object>> listarRutas() {
            return List.of(
                java.util.Map.of("id", java.util.UUID.randomUUID(), "estado", "PLANIFICADA"));
          }
        };

    IncentivosGateway incentivosGateway = new IncentivosGateway(null);
    DonacionesGateway donacionesGateway = new DonacionesGateway(null);

    AdminController controller =
        new AdminController(logisticaGateway, incentivosGateway, donacionesGateway);

    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/templates/");
    viewResolver.setSuffix(".html");

    mockMvc = MockMvcBuilders.standaloneSetup(controller).setViewResolvers(viewResolver).build();
  }

  @Test
  @DisplayName(
      "GET /app/admin/camiones debe retornar 200 OK y la lista de camiones para SLDS Data Table")
  void testListarCamiones() throws Exception {
    mockMvc
        .perform(get("/app/admin/camiones"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/admin/camiones"))
        .andExpect(model().attributeExists("camiones"))
        .andExpect(model().attributeExists("nuevoCamion"));
  }

  @Test
  @DisplayName("POST /app/admin/camiones debe registrar camión y redirigir con flash message")
  void testCrearCamion() throws Exception {
    mockMvc
        .perform(
            post("/app/admin/camiones")
                .param("patente", "CC999DD")
                .param("capacidadVolumen", "30.0")
                .param("altura", "3.8")
                .param("capacidadCarga", "5000.0"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/app/admin/camiones"))
        .andExpect(flash().attributeExists("flashOk"));
  }

  @Test
  @DisplayName("GET /app/admin/matchmaking debe retornar vista con lista de beneficiarios")
  void testMatchmaking() throws Exception {
    mockMvc
        .perform(get("/app/admin/matchmaking"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/admin/matchmaking"))
        .andExpect(model().attributeExists("beneficiarios"));
  }

  @Test
  @DisplayName(
      "GET /app/admin/choferes debe retornar 200 OK y la lista de choferes para SLDS Data Table")
  void testListarChoferes() throws Exception {
    mockMvc
        .perform(get("/app/admin/choferes"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/admin/choferes"))
        .andExpect(model().attributeExists("choferes"))
        .andExpect(model().attributeExists("nuevoChofer"));
  }

  @Test
  @DisplayName("POST /app/admin/choferes debe registrar chofer y redirigir con flash message")
  void testCrearChofer() throws Exception {
    mockMvc
        .perform(
            post("/app/admin/choferes")
                .param("legajo", "CH-202")
                .param("nombre", "Laura")
                .param("apellido", "Fernández")
                .param("telefono", "+54 11 9988-7766"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/app/admin/choferes"))
        .andExpect(flash().attributeExists("flashOk"));
  }

  @Test
  @DisplayName("GET /app/admin/rutas debe retornar 200 OK y la lista de rutas operativas")
  void testListarRutas() throws Exception {
    mockMvc
        .perform(get("/app/admin/rutas"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/admin/rutas"))
        .andExpect(model().attributeExists("rutas"));
  }
}
