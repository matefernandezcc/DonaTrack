package com.donatrack.web.config;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ApiProperties.class)
public class ApiClientsConfig {

  @Bean
  public java.net.http.HttpClient sharedHttpClient() {
    return java.net.http.HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(2000))
        .build();
  }

  @Bean(name = "securityTaskExecutor")
  public AsyncTaskExecutor securityTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(8);
    executor.setMaxPoolSize(16);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("dt-security-io-");
    executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return new DelegatingSecurityContextAsyncTaskExecutor(executor);
  }

  @Bean
  public ClientHttpRequestInterceptor bearerTokenInterceptor(
      @org.springframework.beans.factory.annotation.Value(
              "${INTERNAL_API_KEY:${donatrack.internal-api-key:donatrack-internal-key-change-in-prod}}")
          String internalApiKey) {
    return (request, body, execution) -> {
      request.getHeaders().set("X-Service-Key", internalApiKey);
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth instanceof JwtAuthenticationToken jwtAuth) {
        request.getHeaders().setBearerAuth(jwtAuth.getToken().getTokenValue());
      } else if (auth != null
          && auth.getCredentials() instanceof String token
          && !token.isBlank()) {
        request.getHeaders().setBearerAuth(token);
      }
      return execution.execute(request, body);
    };
  }

  private RestClient createClient(
      String baseUrl,
      ApiProperties props,
      ClientHttpRequestInterceptor interceptor,
      java.net.http.HttpClient sharedHttpClient) {
    long timeoutSec = props.timeoutSeconds() > 0 ? props.timeoutSeconds() : 3;
    JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(sharedHttpClient);
    requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSec));

    return RestClient.builder()
        .baseUrl(baseUrl != null ? baseUrl : "http://localhost:8080")
        .requestFactory(requestFactory)
        .requestInterceptor(interceptor)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  @Bean
  public RestClient authClient(
      ApiProperties props,
      ClientHttpRequestInterceptor interceptor,
      java.net.http.HttpClient sharedHttpClient) {
    return createClient(props.authUrl(), props, interceptor, sharedHttpClient);
  }

  @Bean
  public RestClient logisticaClient(
      ApiProperties props,
      ClientHttpRequestInterceptor interceptor,
      java.net.http.HttpClient sharedHttpClient) {
    return createClient(props.logisticaUrl(), props, interceptor, sharedHttpClient);
  }

  @Bean
  public RestClient incentivosClient(
      ApiProperties props,
      ClientHttpRequestInterceptor interceptor,
      java.net.http.HttpClient sharedHttpClient) {
    return createClient(props.incentivosUrl(), props, interceptor, sharedHttpClient);
  }

  @Bean
  public RestClient donacionesClient(
      ApiProperties props,
      ClientHttpRequestInterceptor interceptor,
      java.net.http.HttpClient sharedHttpClient) {
    return createClient(props.donacionesUrl(), props, interceptor, sharedHttpClient);
  }

  @Bean
  public RestClient notificacionesClient(
      ApiProperties props,
      ClientHttpRequestInterceptor interceptor,
      java.net.http.HttpClient sharedHttpClient) {
    return createClient(props.notificacionesUrl(), props, interceptor, sharedHttpClient);
  }
}
