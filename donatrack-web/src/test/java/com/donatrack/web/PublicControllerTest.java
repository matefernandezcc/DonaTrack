package com.donatrack.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.donatrack.web.controller.PublicController;
import com.donatrack.web.gateway.AuthGateway;
import com.donatrack.web.gateway.IncentivosGateway;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

class PublicControllerTest {

  private MockMvc mockMvc;
  private AuthGateway authGateway;
  private IncentivosGateway incentivosGateway;
  private JwtDecoder jwtDecoder;

  @BeforeEach
  void setUp() {
    authGateway = new AuthGateway(null);
    incentivosGateway = new IncentivosGateway(null) {
      @Override
      public java.util.List<java.util.Map<String, Object>> obtenerTop3() {
        return Collections.emptyList();
      }
    };
    jwtDecoder = token -> null;

    PublicController controller =
        new PublicController(
            authGateway,
            incentivosGateway,
            jwtDecoder,
            new HttpSessionSecurityContextRepository());

    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/templates/");
    viewResolver.setSuffix(".html");

    mockMvc = MockMvcBuilders.standaloneSetup(controller).setViewResolvers(viewResolver).build();
  }

  @Test
  @DisplayName("GET / debe responder 200 OK y retornar la vista pública index")
  void testIndexPublico() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/index"))
        .andExpect(model().attributeExists("ranking"));
  }

  @Test
  @DisplayName("GET /login debe responder 200 OK y retornar la vista pública login")
  void testLoginPage() throws Exception {
    mockMvc.perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/login"));
  }
}

