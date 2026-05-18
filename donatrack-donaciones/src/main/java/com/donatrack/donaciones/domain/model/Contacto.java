package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.MedioContacto;

public class Contacto {
    private String correoElectronico;
    private String telefono;
    private String whatsapp;
    private MedioContacto medioPredeterminado;

    public Contacto(String correoElectronico, String telefono, String whatsapp, MedioContacto medioPredeterminado) {
        this.correoElectronico = correoElectronico;
        this.telefono = telefono;
        this.whatsapp = whatsapp;
        this.medioPredeterminado = medioPredeterminado;
    }

    // Getters y Setters
    public String getCorreoElectronico() {return correoElectronico;}
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico;}

    public String getTelefono() {return telefono;}
    public void setTelefono(String telefono) {this.telefono = telefono;}

    public String getWhatsapp() {return whatsapp;}
    public void setWhatsapp(String whatsapp) {this.whatsapp = whatsapp;}

    public MedioContacto getMedioPredeterminado() {return medioPredeterminado;}
    public void setMedioPredeterminado(MedioContacto medioPredeterminado) {this.medioPredeterminado = medioPredeterminado;}
}