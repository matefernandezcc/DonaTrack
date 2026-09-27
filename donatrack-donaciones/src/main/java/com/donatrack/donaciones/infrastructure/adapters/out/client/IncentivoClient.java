package com.donatrack.donaciones.infrastructure.adapters.out.client;

import com.donatrack.common.dto.ActividadDonacionDTO;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "incentivos", url = "${feign.client.config.incentivos.url:http://incentivos:8001/api}")
public interface IncentivoClient {

  @PostMapping("/donantes/{id}/actividad")
  void registrarActividadDonacionExitosa(
      @PathVariable("id") UUID id, @RequestBody ActividadDonacionDTO actividad);

  @PostMapping("/donantes/{id}/actividad")
  void registrarActividadDonacionEnDeposito(
      @PathVariable("id") UUID id, @RequestBody ActividadDonacionDTO actividad);
}
