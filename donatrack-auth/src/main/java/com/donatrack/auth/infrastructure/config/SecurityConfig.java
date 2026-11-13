package com.donatrack.auth.infrastructure.config;

import com.donatrack.auth.application.service.AuthService;
import com.donatrack.auth.domain.entities.Rol;
import com.donatrack.auth.domain.entities.Usuario;
import com.donatrack.auth.infrastructure.adapters.out.jwt.JwtService;
import com.donatrack.auth.infrastructure.adapters.out.persistence.UsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${donatrack.web-url:http://localhost:8080}")
  private String webUrl = "http://localhost:8080";

  @Value("${INTERNAL_API_KEY:${donatrack.internal-api-key:donatrack-internal-key-change-in-prod}}")
  private String internalApiKey = "donatrack-internal-key-change-in-prod";

  @Bean
  public ClientRegistrationRepository clientRegistrationRepository(
      @Value("${GOOGLE_CLIENT_ID:#{null}}") String googleId,
      @Value("${GOOGLE_CLIENT_SECRET:#{null}}") String googleSecret,
      @Value("${GITHUB_CLIENT_ID:#{null}}") String githubId,
      @Value("${GITHUB_CLIENT_SECRET:#{null}}") String githubSecret) {

    List<ClientRegistration> registrations = new ArrayList<>();

    if (googleId != null
        && !googleId.isBlank()
        && googleSecret != null
        && !googleSecret.isBlank()
        && !googleId.contains("placeholder")) {
      registrations.add(
          CommonOAuth2Provider.GOOGLE
              .getBuilder("google")
              .clientId(googleId.trim())
              .clientSecret(googleSecret.trim())
              .scope("openid", "profile", "email")
              .build());
    }

    if (githubId != null
        && !githubId.isBlank()
        && githubSecret != null
        && !githubSecret.isBlank()
        && !githubId.contains("placeholder")) {
      registrations.add(
          CommonOAuth2Provider.GITHUB
              .getBuilder("github")
              .clientId(githubId.trim())
              .clientSecret(githubSecret.trim())
              .scope("user:email", "read:user")
              .build());
    }

    if (registrations.isEmpty()) {
      registrations.add(
          ClientRegistration.withRegistrationId("disabled")
              .clientId("disabled")
              .clientSecret("disabled")
              .authorizationGrantType(
                  org.springframework.security.oauth2.core.AuthorizationGrantType
                      .AUTHORIZATION_CODE)
              .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
              .authorizationUri("https://disabled.local/auth")
              .tokenUri("https://disabled.local/token")
              .build());
    }

    return new InMemoryClientRegistrationRepository(registrations);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      UsuarioRepository usuarioRepository,
      AuthService authService,
      JwtService jwtService,
      @Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository)
      throws Exception {

    AuthHeaderAuthenticationFilter headerFilter =
        new AuthHeaderAuthenticationFilter(jwtService, internalApiKey);

    http.csrf(AbstractHttpConfigurer::disable)
        .cors(c -> c.configurationSource(corsConfigurationSource()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                    (request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
        .addFilterBefore(headerFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/auth/login",
                        "/auth/register",
                        "/auth/token/exchange",
                        "/api/auth/sso/**",
                        "/healthcheck",
                        "/actuator/**",
                        "/oauth2/**",
                        "/login/**",
                        "/error")
                    .permitAll()
                    .requestMatchers("/admin/usuarios/**")
                    .hasAnyRole("ADMIN", "INTERNAL_SERVICE")
                    .requestMatchers("/api/auth/perfil/**")
                    .authenticated()
                    .anyRequest()
                    .authenticated());

    boolean hasSocialProviders =
        clientRegistrationRepository != null
            && (clientRegistrationRepository.findByRegistrationId("google") != null
                || clientRegistrationRepository.findByRegistrationId("github") != null);

    if (hasSocialProviders) {
      http.oauth2Login(
          oauth2 ->
              oauth2.successHandler(
                  (request, response, authentication) -> {
                    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                    String email = oAuth2User.getAttribute("email");
                    String name = oAuth2User.getAttribute("name");
                    if (email == null) {
                      email = oAuth2User.getAttribute("login") + "@github.user";
                    }
                    if (name == null) {
                      name = email;
                    }

                    // Buscar o crear usuario
                    Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
                    Usuario usuario;
                    if (userOpt.isPresent()) {
                      usuario = userOpt.get();
                      if (usuario.getPersonaId() == null) {
                        usuario.setPersonaId(UUID.randomUUID());
                        usuario = usuarioRepository.save(usuario);
                      }
                    } else {
                      usuario =
                          Usuario.builder()
                              .username(email.split("@")[0].toLowerCase())
                              .password(
                                  new BCryptPasswordEncoder().encode(UUID.randomUUID().toString()))
                              .nombre(name)
                              .email(email)
                              .rol(Rol.DONANTE)
                              .proveedorOAuth("SOCIAL")
                              .personaId(UUID.randomUUID())
                              .build();
                      usuario = usuarioRepository.save(usuario);
                    }

                    String code = authService.generarCodigoIntercambio(usuario);
                    response.sendRedirect(webUrl + "/auth/callback?code=" + code);
                  }));
    }

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    List<String> allowedOrigins =
        Arrays.stream((webUrl != null ? webUrl : "http://localhost:8080").split(","))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .toList();
    configuration.setAllowedOrigins(
        allowedOrigins.isEmpty() ? List.of("http://localhost:8080") : allowedOrigins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Service-Key"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
