package com.donatrack.donaciones.domain.model;

import com.donatrack.donaciones.domain.enums.MedioContacto;
import lombok.Getter;
import lombok.Setter;

public class Contacto {
  @Getter @Setter private String correoElectronico;
  @Getter @Setter private String telefono;
  @Getter @Setter private String whatsapp;
  @Getter @Setter private MedioContacto medioPredeterminado;

  public Contacto(
      String correoElectronico,
      String telefono,
      String whatsapp,
      MedioContacto medioPredeterminado) {
    this.correoElectronico = correoElectronico;
    this.telefono = telefono;
    this.whatsapp = whatsapp;
    this.medioPredeterminado = medioPredeterminado;
  }
}
