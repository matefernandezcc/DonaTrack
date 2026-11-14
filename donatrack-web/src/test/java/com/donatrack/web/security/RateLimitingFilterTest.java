package com.donatrack.web.security;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@DisplayName("RateLimitingFilter — Tests de Control de Tasa de Solicitudes (Bucket4j + Caffeine)")
class RateLimitingFilterTest {

  @RestController
  static class DummyEndpointController {
    @GetMapping("/api/recurso")
    public String recurso() {
      return "OK";
    }

    @GetMapping("/css/styles.css")
    public String css() {
      return "body { background: #fff; }";
    }

    @GetMapping("/actuator/health")
    public String actuatorHealth() {
      return "{\"status\":\"UP\"}";
    }
  }

  private RateLimitingFilter filter;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    // Configura 3 peticiones por minuto para pruebas deterministas
    filter = new RateLimitingFilter(true, 3);
    mockMvc =
        MockMvcBuilders.standaloneSetup(new DummyEndpointController()).addFilters(filter).build();
  }

  @Nested
  @DisplayName("1. Peticiones dentro del límite")
  class PeticionesDentroDelLimite {

    @Test
    @DisplayName("Peticiones admitidas retornan 200 y cabecera X-Rate-Limit-Remaining decrementa")
    void peticionesDentroDelLimiteRetornan200YCabeceraRemaining() throws Exception {
      String ip = "192.168.1.50";

      // Petición 1: consumida, quedan 2 tokens
      mockMvc
          .perform(
              get("/api/recurso")
                  .with(
                      req -> {
                        req.setRemoteAddr(ip);
                        return req;
                      }))
          .andExpect(status().isOk())
          .andExpect(header().string("X-Rate-Limit-Remaining", "2"));

      // Petición 2: consumida, queda 1 token
      mockMvc
          .perform(
              get("/api/recurso")
                  .with(
                      req -> {
                        req.setRemoteAddr(ip);
                        return req;
                      }))
          .andExpect(status().isOk())
          .andExpect(header().string("X-Rate-Limit-Remaining", "1"));

      // Petición 3: consumida, quedan 0 tokens
      mockMvc
          .perform(
              get("/api/recurso")
                  .with(
                      req -> {
                        req.setRemoteAddr(ip);
                        return req;
                      }))
          .andExpect(status().isOk())
          .andExpect(header().string("X-Rate-Limit-Remaining", "0"));
    }
  }

  @Nested
  @DisplayName("2. Ráfaga que excede el límite")
  class RafagaExcedeLimite {

    @Test
    @DisplayName("Petición N+1 retorna 429 Too Many Requests con payload JSON estructurado")
    void peticionNMasUnoRetorna429ConJson() throws Exception {
      String ip = "10.0.0.99";

      // Agotar la cuota de 3 peticiones
      for (int i = 0; i < 3; i++) {
        mockMvc
            .perform(
                get("/api/recurso")
                    .with(
                        req -> {
                          req.setRemoteAddr(ip);
                          return req;
                        }))
            .andExpect(status().isOk());
      }

      // Petición N+1 (4ª petición) debe ser rechazada con HTTP 429
      mockMvc
          .perform(
              get("/api/recurso")
                  .with(
                      req -> {
                        req.setRemoteAddr(ip);
                        return req;
                      }))
          .andExpect(status().isTooManyRequests())
          .andExpect(jsonPath("$.error", is("RATE_LIMIT_EXCEEDED")))
          .andExpect(
              jsonPath(
                  "$.mensaje",
                  is("Demasiadas solicitudes. Por favor, reintente en unos instantes.")));
    }
  }

  @Nested
  @DisplayName("3. Exclusión de rutas estáticas y actuator")
  class ExclusionRutasEstaticas {

    @Test
    @DisplayName("Petición a /css/styles.css no consume tokens del bucket ni añade cabecera")
    void rutasEstaticasNoConsumenTokensDelBucket() throws Exception {
      String ip = "172.16.0.10";

      // Múltiples peticiones a recurso estático (incluso más que el límite de 3)
      for (int i = 0; i < 5; i++) {
        MvcResult result =
            mockMvc
                .perform(
                    get("/css/styles.css")
                        .with(
                            req -> {
                              req.setRemoteAddr(ip);
                              return req;
                            }))
                .andExpect(status().isOk())
                .andReturn();

        // No debe incluir cabecera X-Rate-Limit-Remaining en rutas omitidas
        assertNull(result.getResponse().getHeader("X-Rate-Limit-Remaining"));
      }

      // La siguiente petición normal a /api/recurso debe tener aún la cuota completa intacta
      // (quedan 2 tokens tras consumir 1)
      mockMvc
          .perform(
              get("/api/recurso")
                  .with(
                      req -> {
                        req.setRemoteAddr(ip);
                        return req;
                      }))
          .andExpect(status().isOk())
          .andExpect(header().string("X-Rate-Limit-Remaining", "2"));
    }

    @Test
    @DisplayName("Endpoints de /actuator no sufren bloqueo incluso tras agotarse la cuota de la IP")
    void endpointsActuatorNoSufrenBloqueo() throws Exception {
      String ip = "172.16.0.20";

      // Agotar cuota de la IP
      for (int i = 0; i < 3; i++) {
        mockMvc
            .perform(
                get("/api/recurso")
                    .with(
                        req -> {
                          req.setRemoteAddr(ip);
                          return req;
                        }))
            .andExpect(status().isOk());
      }

      // Petición N+1 a endpoint de API bloqueada
      mockMvc
          .perform(
              get("/api/recurso")
                  .with(
                      req -> {
                        req.setRemoteAddr(ip);
                        return req;
                      }))
          .andExpect(status().isTooManyRequests());

      // Petición a /actuator/health desde la misma IP debe ser admitida sin 429
      mockMvc
          .perform(
              get("/actuator/health")
                  .with(
                      req -> {
                        req.setRemoteAddr(ip);
                        return req;
                      }))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("4. Sanitización y resolución de IP vía X-Forwarded-For")
  class ResolucionIpXForwardedFor {

    @Test
    @DisplayName("Sanitiza lista de proxies en X-Forwarded-For tomando la primera IP")
    void sanitizaListaXForwardedForTomandoPrimeraIp() throws Exception {
      String rawHeader = "203.0.113.195, 70.41.3.18, 150.172.238.178";

      // Agotar 3 tokens usando la cabecera
      for (int i = 0; i < 3; i++) {
        mockMvc
            .perform(
                get("/api/recurso")
                    .header("X-Forwarded-For", rawHeader)
                    .with(
                        req -> {
                          req.setRemoteAddr("10.0.0.1"); // IP del proxy reverso interno
                          return req;
                        }))
            .andExpect(status().isOk());
      }

      // La petición 4 con la misma IP cliente primaria debe fallar
      mockMvc
          .perform(
              get("/api/recurso")
                  .header("X-Forwarded-For", "203.0.113.195, 10.0.0.2")
                  .with(
                      req -> {
                        req.setRemoteAddr("10.0.0.1");
                        return req;
                      }))
          .andExpect(status().isTooManyRequests());

      // Otra IP primaria a través del mismo proxy debe tener su cuota completa
      mockMvc
          .perform(
              get("/api/recurso")
                  .header("X-Forwarded-For", "198.51.100.44, 70.41.3.18")
                  .with(
                      req -> {
                        req.setRemoteAddr("10.0.0.1");
                        return req;
                      }))
          .andExpect(status().isOk())
          .andExpect(header().string("X-Rate-Limit-Remaining", "2"));
    }
  }

  @Nested
  @DisplayName("5. Comportamiento cuando Rate Limiting está deshabilitado")
  class RateLimitingDeshabilitado {

    @Test
    @DisplayName("Si enabled=false, no limita peticiones ni agrega cabecera")
    void conEnabledFalseNoLimitaPeticiones() throws Exception {
      RateLimitingFilter disabledFilter = new RateLimitingFilter(false, 2);
      MockMvc disabledMockMvc =
          MockMvcBuilders.standaloneSetup(new DummyEndpointController())
              .addFilters(disabledFilter)
              .build();

      for (int i = 0; i < 10; i++) {
        disabledMockMvc
            .perform(
                get("/api/recurso")
                    .with(
                        req -> {
                          req.setRemoteAddr("10.10.10.10");
                          return req;
                        }))
            .andExpect(status().isOk());
      }
    }
  }
}
