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

  public Map<String, Object> obtenerBeneficiario(UUID beneficiarioId) {
    try {
      return donacionesClient
          .get()
          .uri("/api/beneficiarios/{id}", beneficiarioId)
          .retrieve()
          .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      log.warn("No se pudo obtener datos del beneficiario {}: {}", beneficiarioId, e.getMessage());
      return Collections.emptyMap();
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
    return sugerirBeneficiarios(donacionId, "HIBRIDO");
  }

  public List<Map<String, Object>> sugerirBeneficiarios(UUID donacionId, String algoritmo) {
    try {
      String alg = (algoritmo != null && !algoritmo.isBlank()) ? algoritmo : "HIBRIDO";
      List<Map<String, Object>> result =
          donacionesClient
              .get()
              .uri("/api/donaciones/{id}/matchmaking?algoritmo={alg}", donacionId, alg)
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.error("Error al consultar matchmaking para donacion {}: {}", donacionId, e.getMessage());
      return Collections.emptyList();
    }
  }

  public List<Map<String, Object>> listarDonaciones(String estado, UUID idDonante) {
    try {
      StringBuilder uri = new StringBuilder("/api/donaciones?");
      if (estado != null && !estado.isBlank()) {
        uri.append("estado=").append(estado).append("&");
      }
      if (idDonante != null) {
        uri.append("idDonante=").append(idDonante).append("&");
      }
      List<Map<String, Object>> result =
          donacionesClient
              .get()
              .uri(uri.toString())
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.warn("Error al consultar listado de donaciones: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  public void asignarDonacion(UUID donacionId, UUID beneficiarioId) {
    donacionesClient
        .put()
        .uri("/api/donaciones/{id}/estado/asignar", donacionId)
        .body(Map.of("id", beneficiarioId.toString()))
        .retrieve()
        .toBodilessEntity();
  }

  public void ejecutarMatchmakingBatch() {
    donacionesClient.post().uri("/api/donaciones/matchmaking/batch").retrieve().toBodilessEntity();
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

  public List<Map<String, Object>> listarSolicitudesOng() {
    try {
      List<Map<String, Object>> result =
          donacionesClient
              .get()
              .uri("/api/solicitudes-ong")
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.error("Error al consultar solicitudes de ONG: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  public Map<String, Object> registrarSolicitudOng(Map<String, ?> data) {
    return donacionesClient
        .post()
        .uri("/api/solicitudes-ong")
        .body(data)
        .retrieve()
        .body(new ParameterizedTypeReference<Map<String, Object>>() {});
  }

  public Map<String, Object> obtenerSolicitudOng(String id) {
    try {
      return donacionesClient
          .get()
          .uri("/api/solicitudes-ong/{id}", id)
          .retrieve()
          .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      log.error("Error al consultar solicitud ONG {}: {}", id, e.getMessage());
      return null;
    }
  }

  public Map<String, Object> actualizarEstadoSolicitudOng(String id, String estado) {
    return donacionesClient
        .put()
        .uri("/api/solicitudes-ong/{id}/estado", id)
        .body(Map.of("estado", estado))
        .retrieve()
        .body(new ParameterizedTypeReference<Map<String, Object>>() {});
  }

  public void marcarDonacionVencida(UUID idDonacion) {
    try {
      donacionesClient
          .put()
          .uri("/api/donaciones/{id}/estado/vencer", idDonacion)
          .retrieve()
          .toBodilessEntity();
    } catch (Exception e) {
      log.error("Error al marcar donacion {} como vencida: {}", idDonacion, e.getMessage());
      throw new RuntimeException("Error al marcar donación como vencida: " + e.getMessage());
    }
  }

  public void auditarVencidos() {
    try {
      donacionesClient
          .post()
          .uri("/api/donaciones/auditoria/vencidos")
          .retrieve()
          .toBodilessEntity();
    } catch (Exception e) {
      log.error("Error al ejecutar auditoria de vencimientos: {}", e.getMessage());
      throw new RuntimeException("Error al ejecutar auditoría de vencimientos: " + e.getMessage());
    }
  }
}
