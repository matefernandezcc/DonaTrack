package com.donatrack.donaciones.infrastructure.adapters.out.client.logistica;

import com.donatrack.donaciones.application.ports.out.LogisticaPort;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogisticaBrokerAdapter implements LogisticaPort {

  private final LogisticaLocalClient localClient;
  private final LogisticaRemoto1Client remoto1Client;
  private final LogisticaRemoto2Client remoto2Client;

  public LogisticaBrokerAdapter(
      LogisticaLocalClient localClient, 
      LogisticaRemoto1Client remoto1Client,
      LogisticaRemoto2Client remoto2Client) {
    this.localClient = localClient;
    this.remoto1Client = remoto1Client;
    this.remoto2Client = remoto2Client;
  }

  @Override
  public void solicitarRetiro(
      UUID idDonacionOriginal,
      double pesoTotal,
      double volumenTotal,
      String calle,
      String altura,
      String localidad) {
    ItemPlanificacionRequest request =
        new ItemPlanificacionRequest(
            idDonacionOriginal, pesoTotal, volumenTotal, calle, altura, localidad);

    try {
      log.info("BROKER: Intentando servicio de logística remoto (Servidor 1) para la donación: {}", idDonacionOriginal);
      remoto1Client.recepcionarDonacionLista(request);
      log.info("BROKER: Éxito con el servicio de logística remoto 1.");
    } catch (Exception e1) {
      log.warn("BROKER: Falló el servicio remoto 1 ({}). Se aplicará Fallback al servicio remoto 2.", e1.getMessage());
      try {
        log.info("BROKER: Intentando servicio de logística remoto (Servidor 2) para la donación: {}", idDonacionOriginal);
        remoto2Client.recepcionarDonacionLista(request);
        log.info("BROKER: Éxito con el servicio de logística remoto 2.");
      } catch (Exception e2) {
        log.warn("BROKER: Falló el servicio remoto 2 ({}). Se aplicará Fallback al servicio local.", e2.getMessage());
        try {
          log.info("BROKER: Intentando servicio de logística local para la donación: {}", idDonacionOriginal);
          localClient.recepcionarDonacionLista(request);
          log.info("BROKER: Éxito con el servicio de logística local.");
        } catch (Exception e3) {
          log.error("BROKER: TODOS los servicios de logística han fallado. No se pudo solicitar el retiro.");
          throw new RuntimeException("Servicios de logística no disponibles en ningún entorno", e3);
        }
      }
    }
  }
}
