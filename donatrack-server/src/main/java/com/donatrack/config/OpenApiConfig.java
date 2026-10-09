package com.donatrack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI donatrackOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("DonaTrack - API Unificada")
                .description(
                    "API Gateway unificado de DonaTrack que integra los módulos de Donaciones, Logística, Incentivos y Notificaciones.")
                .version("1.0.0")
                .contact(new Contact().name("DonaTrack Team").email("team@donatrack.org")));
  }
}
