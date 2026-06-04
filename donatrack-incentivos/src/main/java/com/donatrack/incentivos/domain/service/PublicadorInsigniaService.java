package com.donatrack.incentivos.domain.service;

import com.donatrack.incentivos.domain.model.Insignia;
import com.donatrack.incentivos.domain.model.PerfilDonante;
import org.springframework.stereotype.Service;

@Service
public class PublicadorInsigniaService {

    /**
     * Integración con n8n (herramienta de automatización "low-code").
     * Llama al webhook definido en n8n para generar la imagen con DiceBear y publicarla en redes sociales.
     * @param perfil El donante que obtuvo la insignia
     * @param insignia La insignia obtenida
     */
    public void publicarEnRedesSociales(PerfilDonante perfil, Insignia insignia) {
        // En una implementación real, aquí se usaría un RestTemplate o WebClient
        // para hacer un POST HTTP al webhook de n8n.
        String webhookUrl = "http://localhost:5678/webhook/insignia";
        String payload = String.format("{\"donanteId\": \"%s\", \"insignia\": \"%s\"}", 
            perfil.getDonanteId(), insignia.getNombre());
            
        System.out.println("Llamando a n8n Webhook: " + webhookUrl + " con payload: " + payload);
    }
}
