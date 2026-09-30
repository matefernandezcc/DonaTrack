package com.donatrack.donaciones.infrastructure.adapters.in.api.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactoDTO {
  private String correoElectronico;
  private String telefono;
  private String whatsapp;
  private String medioPredeterminado;
}
