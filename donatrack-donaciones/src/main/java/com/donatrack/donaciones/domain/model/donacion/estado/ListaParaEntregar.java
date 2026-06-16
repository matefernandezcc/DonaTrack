package com.donatrack.donaciones.domain.model.donacion.estado;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.enums.EstadoDonacionEnum;

public class ListaParaEntregar extends EstadoDonacion {

  public ListaParaEntregar(Donacion donacion) {
    super(donacion);
  }

  @Override
  public void iniciarTraslado() {
    donacion.cambiarEstado(new EnTraslado(donacion), "Camión inició el recorrido", null);
  }

  @Override
  public EstadoDonacionEnum getValorEnum() {
    return EstadoDonacionEnum.LISTA_PARA_ENTREGAR;
  }
}
