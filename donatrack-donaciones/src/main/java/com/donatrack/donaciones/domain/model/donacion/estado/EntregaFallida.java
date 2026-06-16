package com.donatrack.donaciones.domain.model.donacion.estado;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.enums.EstadoDonacionEnum;

public class EntregaFallida extends EstadoDonacion {

  public EntregaFallida(Donacion donacion) {
    super(donacion);
  }

  @Override
  public void recibirEnDeposito() {
    donacion.cambiarEstado(new EnDeposito(donacion), "Donación devuelta al depósito luego de una entrega fallida", null);
  }

  @Override
  public EstadoDonacionEnum getValorEnum() {
    return EstadoDonacionEnum.ENTREGA_FALLIDA;
  }
}
