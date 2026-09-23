package com.donatrack.notificaciones.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("notificacionesOpenApiConfig")
public class OpenApiConfig {

  @Bean
  @ConditionalOnMissingBean(OpenAPI.class)
  public OpenAPI notificacionesOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("DonaTrack - API de Notificaciones Multicanal")
            .description("Módulo responsable del envío de alertas y notificaciones multicanal (Email, WhatsApp, Discord vía n8n webhook) ante eventos del sistema.")
            .version("1.0.0")
            .contact(new Contact()
                .name("DonaTrack Team - Notificaciones")
                .email("notificaciones@donatrack.org")));
  }
}
