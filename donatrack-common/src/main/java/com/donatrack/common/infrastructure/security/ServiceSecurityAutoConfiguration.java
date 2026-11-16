package com.donatrack.common.infrastructure.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ServiceSecurityAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(SecurityFilterChain.class)
  public ServiceSecurityFilter serviceSecurityFilter() {
    return new ServiceSecurityFilter();
  }
}
