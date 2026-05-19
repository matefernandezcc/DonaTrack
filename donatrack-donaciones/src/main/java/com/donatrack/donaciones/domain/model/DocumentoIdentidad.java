package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.TipoDocumento;

public class DocumentoIdentidad {
    private TipoDocumento tipo;
    private String numero;

    public DocumentoIdentidad(TipoDocumento tipo, String numero) {
        this.tipo = tipo;
        this.numero = numero;
    }

    // --- Getters y Setters ---

    public TipoDocumento getTipo() { return tipo;}
    public void setTipo(TipoDocumento tipo) { this.tipo = tipo;}

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
}
