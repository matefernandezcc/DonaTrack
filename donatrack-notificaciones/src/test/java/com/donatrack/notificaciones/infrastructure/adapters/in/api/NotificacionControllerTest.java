package com.donatrack.notificaciones.infrastructure.adapters.in.api;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.donatrack.notificaciones.application.usecases.NotificadorService;
import com.donatrack.notificaciones.infrastructure.adapters.out.persistence.entities.NotificacionEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@DisplayName("NotificacionController — Consultas Multi-Destinatario")
class NotificacionControllerTest {

  private MockMvc mockMvc;
  private NotificadorService notificadorService;

  @BeforeEach
  void setUp() {
    notificadorService = mock(NotificadorService.class);
    mockMvc =
        MockMvcBuilders.standaloneSetup(new NotificacionController(notificadorService)).build();
  }

  @Test
  @DisplayName(
      "GET /api/mensajes con múltiples query params destinatario consulta listarPorDestinatarios")
  void testListarMensajesMultiplesDestinatarios() throws Exception {
    NotificacionEntity entidad = new NotificacionEntity();
    entidad.setId(UUID.randomUUID());
    entidad.setDestinatario("real@ejemplo.org");
    entidad.setMensaje("Tu donación fue entregada");
    entidad.setMedio("EMAIL");
    entidad.setCompletada(true);
    entidad.setFechaEnvio(LocalDateTime.now());

    when(notificadorService.listarPorDestinatarios(
            List.of("real@ejemplo.org", "donante123@test.com")))
        .thenReturn(List.of(entidad));

    mockMvc
        .perform(
            get("/api/mensajes")
                .param("destinatario", "real@ejemplo.org")
                .param("destinatario", "donante123@test.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].destinatario").value("real@ejemplo.org"))
        .andExpect(jsonPath("$[0].mensaje").value("Tu donación fue entregada"));

    verify(notificadorService, times(1))
        .listarPorDestinatarios(List.of("real@ejemplo.org", "donante123@test.com"));
  }
}
