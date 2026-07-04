package com.donatrack.logistica.application.ports.in;

import com.donatrack.logistica.domain.entities.planificacion.ItemPlanificacion;

public interface RecepcionarDonacionListaPort {
    void recepcionar(ItemPlanificacion item);
}