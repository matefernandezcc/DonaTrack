package com.donatrack.logistica.infrastructure.adapters.out.storage;

import com.donatrack.logistica.application.ports.out.StoragePort;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class SupabaseS3StorageAdapter implements StoragePort {

  private static final Logger log = LoggerFactory.getLogger(SupabaseS3StorageAdapter.class);

  @Autowired(required = false)
  private S3Client s3Client;

  @Value("${supabase.s3.bucket:remitos}")
  private String bucketName;

  @Value(
      "${supabase.s3.public-url-base:https://nkqyubnzckxojsubyiuk.supabase.co/storage/v1/object/public/remitos}")
  private String publicUrlBase;

  @Override
  public String subirArchivo(String originalFilename, String contentType, byte[] bytes) {
    if (s3Client == null) {
      log.info(
          "Entorno local detectado (S3 deshabilitado). Utilizando remito modelo para desarrollo offline.");
      return "https://nkqyubnzckxojsubyiuk.supabase.co/storage/v1/object/public/remitos/remito-3300.jpg";
    }
    if (bytes == null || bytes.length == 0) {
      log.warn("No se puede subir un archivo vacío a S3.");
      return null;
    }

    try {
      String extension = ".jpg";
      if (originalFilename != null && originalFilename.contains(".")) {
        extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
      }

      String filename =
          "entrega-" + UUID.randomUUID() + "-" + System.currentTimeMillis() + extension;
      String mime = (contentType != null && !contentType.isBlank()) ? contentType : "image/jpeg";

      PutObjectRequest req =
          PutObjectRequest.builder().bucket(bucketName).key(filename).contentType(mime).build();

      s3Client.putObject(req, RequestBody.fromBytes(bytes));
      String publicUrl = publicUrlBase + "/" + filename;
      log.info(
          "Archivo {} subido exitosamente a Supabase S3. URL Pública: {}", filename, publicUrl);
      return publicUrl;
    } catch (Exception e) {
      log.error(
          "Fallo al subir objeto a Supabase S3 (bucket: {}): {}", bucketName, e.getMessage(), e);
      return null;
    }
  }
}
