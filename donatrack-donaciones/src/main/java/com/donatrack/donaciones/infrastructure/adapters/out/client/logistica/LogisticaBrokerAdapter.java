package com.donatrack.donaciones.infrastructure.adapters.out.client.logistica;

import com.donatrack.donaciones.application.ports.out.LogisticaPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class LogisticaBrokerAdapter implements LogisticaPort {

    private final LogisticaLocalClient localClient;
    private final LogisticaRemotoClient remotoClient;

    public LogisticaBrokerAdapter(LogisticaLocalClient localClient, LogisticaRemotoClient remotoClient) {
        this.localClient = localClient;
        this.remotoClient = remotoClient;
    }

    @Override
    public void solicitarRetiro(UUID idDonacionOriginal, double pesoTotal, double volumenTotal, String calle, String altura, String localidad) {
        ItemPlanificacionRequest request = new ItemPlanificacionRequest(
                idDonacionOriginal,
                pesoTotal,
                volumenTotal,
                calle,
                altura,
                localidad
        );

        try {
            log.info("BROKER: Intentando servicio de logística remoto (Nube) para la donación: {}", idDonacionOriginal);
            remotoClient.recepcionarDonacionLista(request);
            log.info("BROKER: Éxito con el servicio de logística remoto.");
        } catch (Exception e) {
            log.warn("BROKER: Falló el servicio remoto ({}). Se aplicará Fallback al servicio local.", e.getMessage());
            try {
                localClient.recepcionarDonacionLista(request);
                log.info("BROKER: Éxito con el servicio de logística local.");
            } catch (Exception ex) {
                log.error("BROKER: Ambos servicios de logística (remoto y local) han fallado. No se pudo solicitar el retiro.");
                throw new RuntimeException("Servicios de logística no disponibles", ex);
            }
        }
    }
}
