package com.donatrack.web.gateway;

import com.donatrack.web.dto.CamionDto;
import com.donatrack.web.dto.ChoferDto;
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
public class LogisticaGateway {

  private static final Logger log = LoggerFactory.getLogger(LogisticaGateway.class);
  private final RestClient logisticaClient;

  public LogisticaGateway(@Qualifier("logisticaClient") RestClient logisticaClient) {
    this.logisticaClient = logisticaClient;
  }

  public List<CamionDto> listarCamiones() {
    try {
      List<CamionDto> result =
          logisticaClient
              .get()
              .uri("/api/camiones")
              .retrieve()
              .body(new ParameterizedTypeReference<List<CamionDto>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.error("Error al consultar camiones de logistica: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  public void crearCamion(CamionDto camion) {
    logisticaClient.post().uri("/api/camiones").body(camion).retrieve().toBodilessEntity();
  }

  public void eliminarCamion(String patente) {
    logisticaClient.delete().uri("/api/camiones/{patente}", patente).retrieve().toBodilessEntity();
  }

  public List<ChoferDto> listarChoferes() {
    try {
      List<ChoferDto> result =
          logisticaClient
              .get()
              .uri("/api/choferes")
              .retrieve()
              .body(new ParameterizedTypeReference<List<ChoferDto>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.error("Error al consultar choferes de logistica: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  public void crearChofer(ChoferDto chofer) {
    logisticaClient.post().uri("/api/choferes").body(chofer).retrieve().toBodilessEntity();
  }

  public void eliminarChofer(String legajo) {
    logisticaClient.delete().uri("/api/choferes/{legajo}", legajo).retrieve().toBodilessEntity();
  }

  public List<Map<String, Object>> listarRutas() {
    try {
      List<Map<String, Object>> result =
          logisticaClient
              .get()
              .uri("/api/rutas")
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.error("Error al consultar rutas de logistica: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  public void confirmarEntrega(UUID idDonacion, List<String> fotos, String patenteCamion) {
    Map<String, Object> body =
        Map.of(
            "fotos", fotos != null ? fotos : List.of(),
            "patenteCamion", patenteCamion != null ? patenteCamion : "");
    logisticaClient
        .post()
        .uri("/api/entregas/{idDonacion}/confirmar", idDonacion)
        .body(body)
        .retrieve()
        .toBodilessEntity();
  }

  public String subirFotoComprobante(UUID idDonacion, MultipartFile file) {
    try {
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("file", file.getResource());

      Map<String, String> resp =
          logisticaClient
              .post()
              .uri("/api/entregas/{idDonacion}/fotos/subir", idDonacion)
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .body(body)
              .retrieve()
              .body(new ParameterizedTypeReference<Map<String, String>>() {});

      return resp != null ? resp.get("url") : null;
    } catch (Exception e) {
      log.error("Error al subir foto de comprobante a logistica/Supabase S3: {}", e.getMessage());
      return null;
    }
  }
}
