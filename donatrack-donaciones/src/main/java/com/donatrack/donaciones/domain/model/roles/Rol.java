package com.donatrack.donaciones.domain.model.roles;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public abstract class Rol {
  @Getter @Setter private UUID id;
  @Getter @Setter private LocalDate fechaAlta;

  public Rol() {
    this.id = UUID.randomUUID();
    this.fechaAlta = LocalDate.now();
  }

  public abstract boolean esValidoParaHumana();
  
  public abstract boolean esValidoParaJuridica();
}
