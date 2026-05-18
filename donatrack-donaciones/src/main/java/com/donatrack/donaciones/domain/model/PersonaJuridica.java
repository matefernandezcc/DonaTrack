package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.TipoJuridica;
import com.donatrack.donaciones.domain.enums.TipoDocumento;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class PersonaJuridica extends Persona {

    private String razonSocial;
    private TipoDocumento tipoDocumento;
    private String cuit;
    private TipoJuridica tipo;
    private String rubro;
    private List<Representante> representantes;

    public PersonaJuridica(String email, Contacto contacto, Direccion direccion, String razonSocial, TipoDocumento tipoDocumento, String cuit, TipoJuridica tipo, String rubro) {
        super(email, contacto, direccion);
        this.razonSocial = razonSocial;
        this.tipoDocumento = tipoDocumento;
        this.cuit = cuit;
        this.tipo = tipo;
        this.rubro = rubro;
        this.representantes = new ArrayList<>();
    }

    public void agregarRepresentante(Representante r) {this.representantes.add(r);}

    @Override protected void validarRol(Rol r) {
        // Regla de Negocio: Tira excepción si el rol es Administrador
        if (r instanceof Administrador) {
            throw new IllegalArgumentException("Error: Una persona jurídica no puede asumir el rol de Administrador.");
        }
    }


    @Override public void actualizarInformacion(Map<String, Object> datosNuevos) {
        super.actualizarInformacion(datosNuevos);
        
        if (datosNuevos.containsKey("razonSocial")) {
            this.razonSocial = (String) datosNuevos.get("razonSocial");
        }
        if (datosNuevos.containsKey("rubro")) {
            this.rubro = (String) datosNuevos.get("rubro");
        }
    }

    // Getters y Setters
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getCuit() { return cuit; }
    public void setCuit(String cuit) { this.cuit = cuit; }

    public TipoJuridica getTipo() { return tipo; }
    public void setTipo(TipoJuridica tipo) { this.tipo = tipo; }

    public String getRubro() { return rubro; }
    public void setRubro(String rubro) { this.rubro = rubro; }

    public List<Representante> getRepresentantes() { return representantes; }
    public void setRepresentantes(List<Representante> representantes) { this.representantes = representantes; }
}