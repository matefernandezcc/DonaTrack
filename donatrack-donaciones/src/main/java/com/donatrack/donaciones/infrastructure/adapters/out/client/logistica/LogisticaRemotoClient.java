package com.donatrack.donaciones.infrastructure.adapters.out.client.logistica;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "logisticaRemotoClient",
    url = "${logistica.url.remota:https://donatrack-logistica-50xn.onrender.com}")
public interface LogisticaRemotoClient {

  @PostMapping("/api/planificacion/items")
  void recepcionarDonacionLista(@RequestBody ItemPlanificacionRequest request);
}
