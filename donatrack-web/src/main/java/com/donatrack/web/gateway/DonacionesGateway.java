package com.donatrack.web.gateway;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DonacionesGateway {

  private static final Logger log = LoggerFactory.getLogger(DonacionesGateway.class);
  private final RestClient donacionesClient;

  public DonacionesGateway(@Qualifier("donacionesClient") RestClient donacionesClient) {
    this.donacionesClient = donacionesClient;
  }

  public Map<String, Object> crearRecepcion(Map<String, Object> payload) {
    return donacionesClient
        .post()
        .uri("/api/recepciones")
        .body(payload)
        .retrieve()
        .body(new ParameterizedTypeReference<Map<String, Object>>() {});
  }

  public List<Map<String, Object>> obtenerBeneficiarios() {
    try {
      List<Map<String, Object>> result =
          donacionesClient
              .get()
              .uri("/api/beneficiarios")
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.error("Error al consultar beneficiarios: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  public List<Map<String, Object>> obtenerNecesidades(UUID beneficiarioId) {
    try {
      List<Map<String, Object>> result =
          donacionesClient
              .get()
              .uri("/api/beneficiarios/{id}/necesidades", beneficiarioId)
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.error("Error al consultar necesidades de {}: {}", beneficiarioId, e.getMessage());
      return Collections.emptyList();
    }
  }

  public void agregarNecesidad(UUID beneficiarioId, Map<String, Object> necesidad) {
    donacionesClient
        .post()
        .uri("/api/beneficiarios/{id}/necesidades", beneficiarioId)
        .body(necesidad)
        .retrieve()
        .toBodilessEntity();
  }

  public List<Map<String, Object>> sugerirBeneficiarios(UUID donacionId) {
    try {
      List<Map<String, Object>> result =
          donacionesClient
              .get()
              .uri("/api/donaciones/{id}/matchmaking", donacionId)
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.error("Error al consultar matchmaking para donacion {}: {}", donacionId, e.getMessage());
      return Collections.emptyList();
    }
  }

  public List<Map<String, Object>> obtenerPersonas() {
    try {
      List<Map<String, Object>> result =
          donacionesClient
              .get()
              .uri("/api/personas")
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.error("Error al consultar personas: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  public String importarCsv(MultipartFile file) {
    try {
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("file", file.getResource());

      return donacionesClient
          .post()
          .uri("/api/personas/importar-csv")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .body(body)
          .retrieve()
          .body(String.class);
    } catch (Exception e) {
      log.error("Error al importar CSV: {}", e.getMessage());
      throw new RuntimeException("Error en importación de CSV: " + e.getMessage());
    }
  }
}
