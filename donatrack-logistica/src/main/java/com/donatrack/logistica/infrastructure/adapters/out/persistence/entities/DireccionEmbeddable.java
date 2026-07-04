package com.donatrack.logistica.infrastructure.adapters.out.persistence.entities;

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
public class DireccionEmbeddable {

  @Column(name = "calle", length = 200)
  private String calle;

  @Column(name = "altura_dir", length = 20)
  private String alturaDir;

  @Column(name = "localidad", length = 100)
  private String localidad;
}
