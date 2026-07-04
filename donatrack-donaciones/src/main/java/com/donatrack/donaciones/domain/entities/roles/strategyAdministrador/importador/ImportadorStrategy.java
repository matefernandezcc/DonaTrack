package com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.importador;

import com.donatrack.donaciones.domain.entities.donacion.Archivo;

public interface ImportadorStrategy {
  void importar(Archivo archivo);
}
