package com.donatrack.logistica.application.ports.out;

public interface StoragePort {

  /**
   * Sube un archivo (comprobante, remito o foto) al almacenamiento de objetos (Supabase S3).
   *
   * @param originalFilename Nombre original del archivo (ej. remito.jpg)
   * @param contentType Tipo MIME (ej. image/jpeg, image/png)
   * @param bytes Contenido binario del archivo
   * @return URL pública absoluta del archivo alojado en el bucket, o null si falló
   */
  String subirArchivo(String originalFilename, String contentType, byte[] bytes);
}

