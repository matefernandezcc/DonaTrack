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

  private static final int MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

  @Override
  public String subirArchivo(String originalFilename, String contentType, byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      log.warn("No se puede subir un archivo vacío a S3.");
      return null;
    }
    if (bytes.length > MAX_FILE_SIZE_BYTES) {
      throw new IllegalArgumentException(
          "La imagen de comprobante supera el límite máximo permitido (5 MB).");
    }

    String detectedExt = detectarExtensionSeguraPorMagicBytes(bytes);
    if (detectedExt == null) {
      throw new IllegalArgumentException(
          "Formato de archivo no permitido. Solo se aceptan imágenes válidas JPEG, PNG o WEBP.");
    }
    String mime =
        switch (detectedExt) {
          case ".jpg" -> "image/jpeg";
          case ".png" -> "image/png";
          default -> "image/webp";
        };

    if (s3Client == null) {
      if (bytes.length <= 2_000_000) {
        log.info(
            "Entorno local detectado (S3 deshabilitado). Codificando foto validada ({}) en data URI base64 ({} bytes).",
            mime,
            bytes.length);
        return "data:" + mime + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
      }
      log.info(
          "Entorno local detectado (S3 deshabilitado). Utilizando remito modelo para desarrollo offline.");
      return "https://nkqyubnzckxojsubyiuk.supabase.co/storage/v1/object/public/remitos/remito-3300.webp";
    }

    try {
      // Nombre estructurado auditado (nunca usa nombre/extensión enviada por el cliente):
      // remito-{timestamp}-{shortUuid}.{ext}
      String shortUuid = UUID.randomUUID().toString().substring(0, 8);
      String filename = "remito-" + System.currentTimeMillis() + "-" + shortUuid + detectedExt;

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
      if (bytes.length <= 2_000_000) {
        return "data:" + mime + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
      }
      return null;
    }
  }

  /**
   * Valida magic bytes binarios para aceptar exclusivamente JPEG, PNG o WEBP (mitiga Stored XSS).
   */
  private String detectarExtensionSeguraPorMagicBytes(byte[] bytes) {
    if (bytes.length >= 3
        && (bytes[0] & 0xFF) == 0xFF
        && (bytes[1] & 0xFF) == 0xD8
        && (bytes[2] & 0xFF) == 0xFF) {
      return ".jpg";
    }
    if (bytes.length >= 8
        && (bytes[0] & 0xFF) == 0x89
        && bytes[1] == 0x50
        && bytes[2] == 0x4E
        && bytes[3] == 0x47
        && bytes[4] == 0x0D
        && bytes[5] == 0x0A
        && bytes[6] == 0x1A
        && bytes[7] == 0x0A) {
      return ".png";
    }
    if (bytes.length >= 12
        && bytes[0] == 'R'
        && bytes[1] == 'I'
        && bytes[2] == 'F'
        && bytes[3] == 'F'
        && bytes[8] == 'W'
        && bytes[9] == 'E'
        && bytes[10] == 'B'
        && bytes[11] == 'P') {
      return ".webp";
    }
    return null;
  }
}
