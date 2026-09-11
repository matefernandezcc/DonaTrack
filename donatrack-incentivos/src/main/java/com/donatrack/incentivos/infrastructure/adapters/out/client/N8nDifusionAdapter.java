package com.donatrack.incentivos.infrastructure.adapters.out.client;

import com.donatrack.incentivos.domain.entities.InsigniaObtenidaEvent;

import com.donatrack.incentivos.application.ports.out.DifusionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class N8nDifusionAdapter implements DifusionPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(N8nDifusionAdapter.class);
    private final RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Value("${n8n.webhook.url:http://n8n:5678/webhook/donatrack/badge-earned}")
    private String webhookUrl;

    public N8nDifusionAdapter() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    @EventListener
    public void difundirInsignia(InsigniaObtenidaEvent evento) {
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
