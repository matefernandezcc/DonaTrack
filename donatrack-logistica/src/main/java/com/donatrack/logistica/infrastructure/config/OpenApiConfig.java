package com.donatrack.logistica.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("logisticaOpenApiConfig")
public class OpenApiConfig {

  @Bean
  @ConditionalOnMissingBean(OpenAPI.class)
  public OpenAPI logisticaOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("DonaTrack - API de Logística y Rutas de Reparto")
            .description("Módulo responsable de la gestión de flota (camiones y choferes), planificación de rutas optimizadas (lotes de ítems y callbacks asíncronos), seguimiento de entregas con fotos de comprobante y reporte de fallas.")
            .version("1.0.0")
            .contact(new Contact()
                .name("DonaTrack Team - Logística")
                .email("logistica@donatrack.org")));
  }
}
