package com.donatrack.web.config;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ApiProperties.class)
public class ApiClientsConfig {

  @Bean
  public ClientHttpRequestInterceptor bearerTokenInterceptor() {
    return (request, body, execution) -> {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth instanceof JwtAuthenticationToken jwtAuth) {
        request.getHeaders().setBearerAuth(jwtAuth.getToken().getTokenValue());
      } else if (auth != null && auth.getCredentials() instanceof String token && !token.isBlank()) {
        request.getHeaders().setBearerAuth(token);
      }
      return execution.execute(request, body);
    };
  }

  private RestClient createClient(
      String baseUrl, ApiProperties props, ClientHttpRequestInterceptor interceptor) {
    JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
    requestFactory.setReadTimeout(Duration.ofSeconds(props.timeoutSeconds() > 0 ? props.timeoutSeconds() : 60));

    return RestClient.builder()
        .baseUrl(baseUrl != null ? baseUrl : "http://localhost:8080")
        .requestFactory(requestFactory)
        .requestInterceptor(interceptor)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  @Bean
  public RestClient authClient(ApiProperties props, ClientHttpRequestInterceptor interceptor) {
    return createClient(props.authUrl(), props, interceptor);
  }

  @Bean
  public RestClient logisticaClient(ApiProperties props, ClientHttpRequestInterceptor interceptor) {
    return createClient(props.logisticaUrl(), props, interceptor);
  }

  @Bean
  public RestClient incentivosClient(ApiProperties props, ClientHttpRequestInterceptor interceptor) {
    return createClient(props.incentivosUrl(), props, interceptor);
  }

  @Bean
  public RestClient donacionesClient(ApiProperties props, ClientHttpRequestInterceptor interceptor) {
    return createClient(props.donacionesUrl(), props, interceptor);
  }
}

