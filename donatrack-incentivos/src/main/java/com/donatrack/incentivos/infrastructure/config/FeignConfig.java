package com.donatrack.incentivos.infrastructure.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

  @Bean
  public RequestInterceptor serviceKeyRequestInterceptor(
      @Value(
              "${INTERNAL_API_KEY:${donatrack.internal-api-key:donatrack-internal-key-change-in-prod}}")
          String internalApiKey) {
    return template -> template.header("X-Service-Key", internalApiKey);
  }
}
