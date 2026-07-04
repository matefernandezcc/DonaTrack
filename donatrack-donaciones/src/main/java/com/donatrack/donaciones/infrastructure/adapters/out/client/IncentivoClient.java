package com.donatrack.donaciones.infrastructure.adapters.out.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.UUID;
import org.springframework.web.bind.annotation.RequestBody;
import com.donatrack.common.dto.ActividadDonacionDTO;

@FeignClient(name = "incentivos", url = "http://localhost:8080/incentivos")
public interface IncentivoClient {

    @PostMapping("/donantes/{id}/actividad-entregada")
    void registrarActividadDonacionExitosa(@PathVariable("id") UUID id, @RequestBody ActividadDonacionDTO actividad);

    @PostMapping("/entidades/{id}/actividad-deposito")
    void registrarActividadDonacionEnDeposito(@PathVariable("id") UUID id, @RequestBody ActividadDonacionDTO actividad);
}
