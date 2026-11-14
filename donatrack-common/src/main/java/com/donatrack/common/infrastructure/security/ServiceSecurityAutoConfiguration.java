package com.donatrack.common.infrastructure.security;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnMissingBean(SecurityFilterChain.class)
public class ServiceSecurityAutoConfiguration {

  private static final Logger log =
      LoggerFactory.getLogger(ServiceSecurityAutoConfiguration.class);

  @Autowired private Environment environment;

  @Value(
      "${donatrack.internal-api-key:${INTERNAL_API_KEY:donatrack-internal-key-change-in-prod}}")
  private String internalApiKey;

  @PostConstruct
  public void validarSeguridadClaveM2M() {
    boolean esProd =
        environment != null && Arrays.asList(environment.getActiveProfiles()).contains("prod");
    String claveInsegura = "donatrack-internal-key-change-in-prod";
    if (esProd
        && (claveInsegura.equals(internalApiKey)
            || internalApiKey == null
            || internalApiKey.length() < 32)) {
      throw new IllegalStateException(
          "CRÍTICO: INTERNAL_API_KEY insegura en producción. Debe poseer al menos 32 caracteres y diferir del valor por defecto.");
    }
    if (claveInsegura.equals(internalApiKey)) {
      log.warn(
          "ADVERTENCIA DE SEGURIDAD: INTERNAL_API_KEY utiliza la clave por defecto. Válido únicamente en entorno local/test.");
    }
  }

  @Bean
  public ServiceSecurityFilter serviceSecurityFilter() {
    return new ServiceSecurityFilter();
  }
}
