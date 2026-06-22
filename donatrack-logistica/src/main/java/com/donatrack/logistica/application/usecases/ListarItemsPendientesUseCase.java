package com.donatrack.logistica.application.usecases;

import com.donatrack.logistica.application.ports.in.ListarItemsPendientesPort;
import com.donatrack.logistica.application.ports.out.ItemPlanificacionRepositoryPort;
import com.donatrack.logistica.domain.entities.ItemPlanificacion;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarItemsPendientesUseCase implements ListarItemsPendientesPort {

    private final ItemPlanificacionRepositoryPort repositoryPort;

    // Constructor para inyectar la dependencia (el puerto de salida)
    public ListarItemsPendientesUseCase(ItemPlanificacionRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    // Cumplimos con el contrato del puerto de entrada
    @Override
    public List<ItemPlanificacion> listar() {
        // Delegamos el trabajo sucio al puerto de salida
        return repositoryPort.obtenerTodos();
    }
}