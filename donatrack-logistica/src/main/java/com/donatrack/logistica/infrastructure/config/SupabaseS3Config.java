package com.donatrack.logistica.infrastructure.config;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class SupabaseS3Config {

  private static final Logger log = LoggerFactory.getLogger(SupabaseS3Config.class);

  @Value("${supabase.s3.enabled:false}")
  private boolean enabled;

  @Value("${supabase.s3.endpoint:https://nkqyubnzckxojsubyiuk.storage.supabase.co/storage/v1/s3}")
  private String endpoint;

  @Value("${supabase.s3.region:us-east-1}")
  private String region;

  @Value("${supabase.s3.access-key-id:}")
  private String accessKey;

  @Value("${supabase.s3.secret-access-key:}")
  private String secretKey;

  @Bean
  public S3Client s3Client() {
    if (!enabled) {
      log.info("Supabase S3 deshabilitado en este entorno (dev/local). Se utilizará almacenamiento local sin llamadas a S3.");
      return null;
    }
    if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
      log.warn("Supabase S3: Claves de acceso no configuradas. El cliente S3 no estará disponible.");
      return null;
    }
    log.info("Inicializando cliente S3 de Supabase Storage en endpoint: {} (región: {})", endpoint, region);
    return S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
        .build();
  }
}

