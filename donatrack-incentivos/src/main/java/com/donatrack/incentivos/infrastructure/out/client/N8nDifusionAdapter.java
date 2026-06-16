package com.donatrack.incentivos.infrastructure.out.client;

import com.donatrack.incentivos.domain.model.InsigniaObtenidaEvent;
import com.donatrack.incentivos.domain.model.PerfilDonante;
import com.donatrack.incentivos.domain.service.DifusionAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class N8nDifusionAdapter implements DifusionAdapter {

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

        System.out.println("Enviando evento de insignia a n8n: " + payload);
        try {
            restTemplate.postForObject(webhookUrl, payload, String.class);
            System.out.println("Evento enviado exitosamente a n8n.");
        } catch (RestClientException e) {
            System.err.println("No se pudo conectar con n8n para difundir insignia (Tolerancia a fallos): " + e.getMessage());
        }
    }

}
