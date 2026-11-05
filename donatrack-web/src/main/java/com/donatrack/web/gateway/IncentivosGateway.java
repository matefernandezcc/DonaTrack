package com.donatrack.web.gateway;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class IncentivosGateway {

  private static final Logger log = LoggerFactory.getLogger(IncentivosGateway.class);
  private final RestClient incentivosClient;

  public IncentivosGateway(@Qualifier("incentivosClient") RestClient incentivosClient) {
    this.incentivosClient = incentivosClient;
  }

  public List<Map<String, Object>> obtenerTop3() {
    try {
      List<Map<String, Object>> result =
          incentivosClient
              .get()
              .uri("/api/ranking/top3")
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.error("Error al consultar ranking top 3 de incentivos: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  public Map<String, Object> obtenerMetricas(UUID donanteId) {
    try {
      return incentivosClient
          .get()
          .uri("/api/donantes/{id}/metricas", donanteId)
          .retrieve()
          .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      log.error("Error al consultar metricas del donante {}: {}", donanteId, e.getMessage());
      return Collections.emptyMap();
    }
  }

  public List<Map<String, Object>> obtenerMisiones(UUID donanteId) {
    try {
      List<Map<String, Object>> result =
          incentivosClient
              .get()
              .uri("/api/donantes/{id}/misiones", donanteId)
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.error("Error al consultar misiones del donante {}: {}", donanteId, e.getMessage());
      return Collections.emptyList();
    }
  }

  public List<Map<String, Object>> obtenerInsignias(UUID donanteId) {
    try {
      List<Map<String, Object>> result =
          incentivosClient
              .get()
              .uri("/api/donantes/{id}/insignias", donanteId)
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.error("Error al consultar insignias del donante {}: {}", donanteId, e.getMessage());
      return Collections.emptyList();
    }
  }
}
