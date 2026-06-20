package com.donatrack.donaciones.domain.model.persona;

import com.donatrack.donaciones.domain.model.enums.MedioContacto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Contacto {
  private String correoElectronico;
  private String telefono;
  private String whatsapp;
  private MedioContacto medioPredeterminado;

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
