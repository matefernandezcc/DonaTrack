package com.donatrack.notificaciones.infrastructure.adapters.out.messaging;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.donatrack.notificaciones.domain.entities.Notificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

class AdaptadorDiscordTest {

  private RestTemplate restTemplate;
  private AdaptadorDiscord adaptadorDiscord;

  @BeforeEach
  void setUp() {
    restTemplate = Mockito.mock(RestTemplate.class);
    adaptadorDiscord = new AdaptadorDiscord(restTemplate);
    ReflectionTestUtils.setField(
        adaptadorDiscord, "n8nWebhookUrl", "http://n8n:5678/webhook/donatrack/notificaciones");
    ReflectionTestUtils.setField(
        adaptadorDiscord, "discordWebhookUrl", "https://discord.com/api/webhooks/mock");
  }

  @Test
  @DisplayName("Debe enviar la notificación a través de n8n webhook exitosamente")
  void testEnviarViaN8nExitoso() {
    Notificacion notificacion = new Notificacion("Mateo", "Donación asignada");

    when(restTemplate.postForObject(
            eq("http://n8n:5678/webhook/donatrack/notificaciones"), any(), eq(String.class)))
        .thenReturn("{\"message\":\"Workflow was started\"}");

    adaptadorDiscord.enviar(notificacion);

    assertTrue(notificacion.isCompletada());
    verify(restTemplate)
        .postForObject(
            eq("http://n8n:5678/webhook/donatrack/notificaciones"), any(), eq(String.class));
  }
}
