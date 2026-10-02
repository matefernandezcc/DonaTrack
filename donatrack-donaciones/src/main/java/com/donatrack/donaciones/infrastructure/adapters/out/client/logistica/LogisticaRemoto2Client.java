package com.donatrack.donaciones.infrastructure.adapters.out.client.logistica;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "logisticaRemoto2Client",
    url = "${logistica.url.remota2:https://dona-logistica.onrender.com}")
public interface LogisticaRemoto2Client {

  @PostMapping("/api/planificacion/items")
  void recepcionarDonacionLista(@RequestBody ItemPlanificacionRequest request);
}
