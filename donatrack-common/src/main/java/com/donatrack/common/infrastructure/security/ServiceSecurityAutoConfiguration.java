package com.donatrack.common.infrastructure.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ServiceSecurityAutoConfiguration {

  @Bean
  public ServiceSecurityFilter serviceSecurityFilter() {
    return new ServiceSecurityFilter();
  }
}
