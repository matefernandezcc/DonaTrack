package com.donatrack.donaciones.domain.model.donacion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Foto {
  private String descripcion;
  private String url;

  public Foto(String descripcion, String url) {
    this.descripcion = descripcion;
    this.url = url;
  }
}
