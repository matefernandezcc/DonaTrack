package com.donatrack.notificaciones.domain.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Evento {
  private TipoEvento tipoEvento;
  private String descripcion;
  private List<String> rolesDestinatarios;

  public boolean validoParaNotificar(String rol) {
    return rolesDestinatarios != null && rolesDestinatarios.contains(rol);
  }
}
