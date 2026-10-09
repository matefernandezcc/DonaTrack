package com.donatrack.donaciones.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("donacionesOpenApiConfig")
public class OpenApiConfig {

  @Bean
  @ConditionalOnMissingBean(OpenAPI.class)
  public OpenAPI donacionesOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("DonaTrack - API de Donaciones, Beneficiarios y Personas")
                .description(
                    "Módulo responsable de la recepción de donaciones, gestión de personas (humanas y jurídicas), importación masiva por CSV, administración de beneficiarios con sus necesidades y matchmaking inteligente de asignación.")
                .version("1.0.0")
                .contact(
                    new Contact()
                        .name("DonaTrack Team - Donaciones")
                        .email("donaciones@donatrack.org")));
  }
}
