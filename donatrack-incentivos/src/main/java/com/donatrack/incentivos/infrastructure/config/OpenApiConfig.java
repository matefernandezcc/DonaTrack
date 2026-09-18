package com.donatrack.incentivos.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI incentivosOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("DonaTrack - API de Incentivos, Gamificación y Ranking")
            .description("Módulo responsable del registro de actividades de donantes, cálculo de métricas de impacto, asignación de insignias/medallas por logros y generación de rankings (Top 3 donantes).")
            .version("1.0.0")
            .contact(new Contact()
                .name("DonaTrack Team - Incentivos")
                .email("incentivos@donatrack.org")));
  }
}
