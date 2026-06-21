package com.donatrack.incentivos.infrastructure.out.client;

import com.donatrack.incentivos.domain.entities.InsigniaObtenidaEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class N8nDifusionAdapter implements DifusionAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(N8nDifusionAdapter.class);
    private final RestTemplate restTemplate;

    public N8nDifusionAdapter() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    @EventListener
    public void difundirInsignia(InsigniaObtenidaEvent evento) {
        String webhookUrl = "http://localhost:5678/webhook/donatrack/badge-earned";
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("user", evento.getDonanteId().toString());
        payload.put("badge", evento.getInsignia().getNombre());
        payload.put("description", evento.getInsignia().getDescripcion());

        LOGGER.info("Enviando evento de insignia a n8n: {}", payload);
        try {
            restTemplate.postForObject(webhookUrl, payload, String.class);
            LOGGER.info("Evento enviado exitosamente a n8n.");
        } catch (RestClientException e) {
            LOGGER.error("No se pudo conectar con n8n para difundir insignia (Tolerancia a fallos): {}", e.getMessage());
        }
    }

}
