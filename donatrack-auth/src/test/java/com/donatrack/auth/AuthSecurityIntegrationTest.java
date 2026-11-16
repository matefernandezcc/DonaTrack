package com.donatrack.auth;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.donatrack.auth.domain.entities.Rol;
import com.donatrack.auth.domain.entities.Usuario;
import com.donatrack.auth.infrastructure.adapters.out.jwt.JwtService;
import com.donatrack.auth.infrastructure.config.AuthDataSeeder;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = DonatrackAuthApplication.class,
    properties = {
      "spring.datasource.url=jdbc:h2:mem:authsec;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS auth",
      "spring.datasource.driverClassName=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true",
      "spring.jpa.properties.hibernate.default_schema=auth",
      "donatrack.security.enabled=true",
      "donatrack.internal-api-key=test-auth-internal-key",
      "donatrack.jwt-secret=SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!",
      "donatrack.web-url=https://web.donatrack.test"
    })
@ActiveProfiles("prod")
@AutoConfigureMockMvc
@DisplayName("AuthSecurityIntegrationTest — Seguridad P0 en donatrack-auth (perfil prod)")
class AuthSecurityIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private JwtService jwtService;

  @Autowired(required = false)
  private AuthDataSeeder authDataSeeder;

  @Test
  @DisplayName("AuthDataSeeder no debe estar activo en perfil prod")
  void testSeederDesactivadoEnProd() {
    assertNull(authDataSeeder, "AuthDataSeeder no debe cargarse fuera de local/dev/test");
  }

  @Test
  @DisplayName("GET /admin/usuarios sin credenciales retorna 401")
  void testAdminUsuariosAnonimoRetorna401() throws Exception {
    mockMvc.perform(get("/admin/usuarios")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET /api/auth/perfil/{id} sin credenciales retorna 401")
  void testPerfilAnonimoRetorna401() throws Exception {
    mockMvc
        .perform(get("/api/auth/perfil/{id}", UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET /admin/usuarios con JWT de DONANTE retorna 403")
  void testAdminUsuariosConJwtDonanteRetorna403() throws Exception {
    Usuario donante =
        Usuario.builder()
            .id(UUID.randomUUID())
            .username("donante1")
            .email("donante1@ejemplo.org")
            .rol(Rol.DONANTE)
            .build();
    String token = jwtService.generarToken(donante);

    mockMvc
        .perform(get("/admin/usuarios").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /admin/usuarios con X-Service-Key válida retorna 200")
  void testAdminUsuariosConServiceKeyRetorna200() throws Exception {
    mockMvc
        .perform(get("/admin/usuarios").header("X-Service-Key", "test-auth-internal-key"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /admin/usuarios con JWT de ADMIN retorna 200")
  void testAdminUsuariosConJwtAdminRetorna200() throws Exception {
    Usuario admin =
        Usuario.builder()
            .id(UUID.randomUUID())
            .username("admin1")
            .email("admin1@ejemplo.org")
            .rol(Rol.ADMIN)
            .build();
    String token = jwtService.generarToken(admin);

    mockMvc
        .perform(get("/admin/usuarios").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("CORS permite origen configurado en donatrack.web-url y rechaza orígenes externos")
  void testCorsRestringidoAlWebUrl() throws Exception {
    mockMvc
        .perform(
            options("/api/auth/login")
                .header("Origin", "https://web.donatrack.test")
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(header().string("Access-Control-Allow-Origin", "https://web.donatrack.test"));

    mockMvc
        .perform(
            options("/api/auth/login")
                .header("Origin", "https://evil.example.com")
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
  }
}
