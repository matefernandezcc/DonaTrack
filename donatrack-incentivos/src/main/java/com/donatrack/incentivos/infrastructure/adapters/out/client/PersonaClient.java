package com.donatrack.incentivos.infrastructure.adapters.out.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "personasClient", url = "${personas.api.url:http://donaciones:8000}")
public interface PersonaClient {

    @GetMapping("/api/personas/{id}")
    PersonaDTO obtenerPersona(@PathVariable("id") UUID id);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PersonaDTO(String nombre, String apellido, String razonSocial, String tipo) {}
}
