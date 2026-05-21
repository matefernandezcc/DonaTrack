package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.TipoJuridica;

import lombok.Getter;
import lombok.Setter;

import com.donatrack.donaciones.domain.enums.TipoDocumento;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class PersonaJuridica extends Persona {

    @Getter @Setter private String razonSocial;
    @Getter @Setter private TipoDocumento tipoDocumento;
    @Getter @Setter private String cuit;
    @Getter @Setter private TipoJuridica tipo;
    @Getter @Setter private String rubro;
    @Getter @Setter private List<Representante> representantes;

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
}