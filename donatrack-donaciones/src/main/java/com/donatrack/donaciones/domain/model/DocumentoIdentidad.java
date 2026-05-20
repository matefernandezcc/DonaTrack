package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.TipoDocumento;

import lombok.Getter;
import lombok.Setter;

public class DocumentoIdentidad {
    @Getter @Setter private TipoDocumento tipo;
    @Getter @Setter private String numero;

    public DocumentoIdentidad(TipoDocumento tipo, String numero) {
        this.tipo = tipo;
        this.numero = numero;
    }
}
