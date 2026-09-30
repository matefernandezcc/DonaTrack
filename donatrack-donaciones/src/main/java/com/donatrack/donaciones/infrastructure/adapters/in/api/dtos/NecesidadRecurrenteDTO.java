package com.donatrack.donaciones.infrastructure.adapters.in.api.dtos;

import com.donatrack.donaciones.domain.entities.necesidades.TipoPeriodo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadRecurrenteDTO extends NecesidadDTO {
  private Double cantidadObjetivo;
  private TipoPeriodo tipoPeriodo;
  private Boolean activa;
}
