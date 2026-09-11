package com.donatrack.donaciones.infrastructure.adapters.out.client.logistica;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "logisticaRemotoClient", url = "${logistica.url.remota:https://dona-logistica.onrender.com}")
public interface LogisticaRemotoClient {

    @PostMapping("/api/planificacion/items")
    void recepcionarDonacionLista(@RequestBody ItemPlanificacionRequest request);
}
