package com.donatrack.donaciones.domain.model.donacion.estado;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.enums.EstadoDonacionEnum;

public class EnTraslado extends EstadoDonacion {

  public EnTraslado(Donacion donacion) {
    super(donacion);
  }

  @Override
  public void entregar() {
    donacion.cambiarEstado(new Entregada(donacion), "Donación entregada y confirmada por entidad beneficiaria", null);
  }

  @Override
  public void fallarEntrega(String justificacion) {
    donacion.cambiarEstado(new EntregaFallida(donacion), justificacion, null);
  }

  @Override
  public EstadoDonacionEnum getValorEnum() {
    return EstadoDonacionEnum.EN_TRASLADO;
  }
}
