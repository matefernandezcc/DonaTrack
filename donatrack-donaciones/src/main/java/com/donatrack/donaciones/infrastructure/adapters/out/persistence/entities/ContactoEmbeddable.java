package com.donatrack.donaciones.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactoEmbeddable {

  @Column(name = "contacto_medio")
  private String medio;

  @Column(name = "contacto_correo")
  private String correo;

  @Column(name = "contacto_telefono")
  private String telefono;

  @Column(name = "contacto_whatsapp")
  private String whatsapp;
}
