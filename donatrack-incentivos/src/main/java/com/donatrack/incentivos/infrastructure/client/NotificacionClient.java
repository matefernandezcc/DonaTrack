package com.donatrack.incentivos.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notificaciones-incentivos", url = "http://localhost:8080/notificaciones")
public interface NotificacionClient {

    @PostMapping("/enviar")
    void enviarNotificacion(@RequestBody NotificacionRequest request);
}
