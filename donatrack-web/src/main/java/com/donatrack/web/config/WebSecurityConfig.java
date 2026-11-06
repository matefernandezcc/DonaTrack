package com.donatrack.web.config;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

  @Value("${donatrack.jwt-secret}")
  private String jwtSecret;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .securityContext(sc -> sc.securityContextRepository(securityContextRepository()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/",
                        "/login",
                        "/registro",
                        "/auth/**",
                        "/oauth2/**",
                        "/api/public/**",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/remitos/**",
                        "/fotos/**",
                        "/assets/**",
                        "/favicon.svg",
                        "/healthcheck",
                        "/actuator/**",
                        "/error")
                    .permitAll()
                    .requestMatchers("/app/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(
            logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID"));

    return http.build();
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
  }

  @Bean
  public SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }
}
