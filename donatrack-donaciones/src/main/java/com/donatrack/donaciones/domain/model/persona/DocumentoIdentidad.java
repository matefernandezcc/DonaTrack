package com.donatrack.donaciones.domain.model.persona;

import com.donatrack.donaciones.domain.model.enums.TipoDocumento;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentoIdentidad {
  private TipoDocumento tipo;
  private String numero;

  public DocumentoIdentidad(TipoDocumento tipo, String numero) {
    this.tipo = tipo;
    this.numero = numero;
  }
}
