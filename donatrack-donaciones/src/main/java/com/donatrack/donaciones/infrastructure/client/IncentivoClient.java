package com.donatrack.donaciones.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.UUID;

@FeignClient(name = "incentivos", url = "http://localhost:8080/api/incentivos")
public interface IncentivoClient {

    @PostMapping("/donantes/{id}/actividad")
    void registrarActividadDonacionExitosa(@PathVariable UUID id);
}
