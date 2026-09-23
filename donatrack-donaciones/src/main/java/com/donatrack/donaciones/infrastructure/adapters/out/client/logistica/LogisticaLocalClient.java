package com.donatrack.donaciones.infrastructure.adapters.out.client.logistica;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "logisticaLocalClient", url = "${logistica.url.local:http://localhost:8002}")
public interface LogisticaLocalClient {

    @PostMapping("/api/planificacion/items")
    void recepcionarDonacionLista(@RequestBody ItemPlanificacionRequest request);
}
