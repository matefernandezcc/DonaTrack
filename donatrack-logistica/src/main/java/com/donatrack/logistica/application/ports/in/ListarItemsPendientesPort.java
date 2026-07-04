package com.donatrack.logistica.application.ports.in;

import java.util.List;

import com.donatrack.logistica.domain.entities.planificacion.ItemPlanificacion;

public interface ListarItemsPendientesPort {
    List<ItemPlanificacion> listar();
}