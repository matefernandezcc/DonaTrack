package com.donatrack.logistica.application.usecases;

import com.donatrack.logistica.application.ports.in.RecepcionarDonacionListaPort;
import com.donatrack.logistica.application.ports.out.ItemPlanificacionRepositoryPort;
import com.donatrack.logistica.domain.entities.ItemPlanificacion;
import org.springframework.stereotype.Service;

@Service
public class RecepcionarDonacionListaUseCase implements RecepcionarDonacionListaPort {

    private final ItemPlanificacionRepositoryPort repositoryPort;

    public RecepcionarDonacionListaUseCase(ItemPlanificacionRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public void recepcionar(ItemPlanificacion item) {
        // Acá a futuro podrías validar reglas de negocio, como que el peso no sea
        // negativo
        repositoryPort.guardar(item);
    }
}