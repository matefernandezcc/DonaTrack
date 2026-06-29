package com.donatrack.logistica.infrastructure.adapters.in.api;

import com.donatrack.logistica.application.ports.in.ListarItemsPendientesPort;
import com.donatrack.logistica.domain.entities.ItemPlanificacion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LogisticaController {

    private final ListarItemsPendientesPort listarItemsPendientesPort;

    public LogisticaController(ListarItemsPendientesPort listarItemsPendientesPort) {
        this.listarItemsPendientesPort = listarItemsPendientesPort;
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<ItemPlanificacion>> obtenerTodos() {
        return ResponseEntity.ok(listarItemsPendientesPort.listar());
    }
}
