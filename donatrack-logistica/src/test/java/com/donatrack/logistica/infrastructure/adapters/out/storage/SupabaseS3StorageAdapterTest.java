package com.donatrack.logistica.infrastructure.adapters.out.storage;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SupabaseS3StorageAdapterTest {

  private final SupabaseS3StorageAdapter adapter = new SupabaseS3StorageAdapter();

  @Test
  @DisplayName("Rechaza archivos HTML/SVG falsificados por magic bytes (prevención Stored XSS)")
  void rechazaArchivoHtmlDisfrazadoDeImagen() {
    byte[] htmlPayload = "<script>alert('xss')</script>".getBytes(StandardCharsets.UTF_8);
    assertThrows(
        IllegalArgumentException.class,
        () -> adapter.subirArchivo("comprobante.png", "image/png", htmlPayload));
  }

  @Test
  @DisplayName("Acepta imagen PNG válida verificando magic bytes")
  void aceptaPngValido() {
    byte[] validPngHeader =
        new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00};
    String result = adapter.subirArchivo("evil.html", "text/html", validPngHeader);
    assertTrue(result.startsWith("data:image/png;base64,"));
  }
}
