package com.donatrack.donaciones.domain.entities.roles;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Rol {
  private UUID id;
  private LocalDate fechaAlta;

  protected Rol() {
    this.id = UUID.randomUUID();
    this.fechaAlta = LocalDate.now();
  }

  public abstract boolean esValidoParaHumana();
  
  public abstract boolean esValidoParaJuridica();
}
